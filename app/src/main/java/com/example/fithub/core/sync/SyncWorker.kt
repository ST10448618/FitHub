package com.example.fithub.core.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.fithub.core.LogTags
import com.example.fithub.core.SessionManager
import com.example.fithub.core.ServiceLocator
import java.util.concurrent.TimeUnit

/**
 * Reference: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
 *            https://developer.android.com/reference/androidx/work/ExistingWorkPolicy
 * (accessed February 2026)
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = SessionManager.currentUserId
        if (uid == null) {
            Log.d(LogTags.SYNC, "SyncWorker: no session — success (nothing to do)")
            return Result.success()
        }
        val report = runCatching { ServiceLocator.syncManager.flush(uid) }
            .onFailure { Log.e(LogTags.SYNC, "SyncWorker flush threw", it) }
            .getOrNull()

        if (report == null) return Result.retry()

        return when {
            report.abortedOffline -> {
                Log.w(LogTags.SYNC, "SyncWorker: offline — retry")
                Result.retry()
            }
            report.failed > 0 -> {
                Log.w(LogTags.SYNC, "SyncWorker: ${report.failed} failures — retry (remaining=${report.remaining})")
                Result.retry()
            }
            else -> {
                Log.i(LogTags.SYNC, "SyncWorker: clean (pushed=${report.pushed})")
                Result.success()
            }
        }
    }

    companion object {
        private const val NAME = "fithub_sync"

        /** Fire-and-forget enqueue with a network constraint and exponential backoff. */
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    15L, TimeUnit.SECONDS
                )
                .setInputData(workDataOf("trigger" to "enqueue"))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
            Log.d(LogTags.SYNC, "SyncWorker enqueued (unique=$NAME)")
        }
    }
}

/**
 * Convenience façade the repositories call after an enqueue. Kept separate from
 * [SyncWorker] so that unit tests can call the repositories without depending
 * on WorkManager at all.
 */
object SyncScheduler {
    fun enqueue(context: android.content.Context) = SyncWorker.enqueue(context)
}