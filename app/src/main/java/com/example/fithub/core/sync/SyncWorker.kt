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
 * Background sync worker.
 *
 * ### Note on locale (Phase 5)
 * `CoroutineWorker` extends `ListenableWorker`, which does **not** extend
 * `android.content.Context`. Therefore `ContextWrapper.attachBaseContext` is
 * unavailable here — it only exists on `Context` subclasses (Activity,
 * Service, Application, etc.). When a future worker needs a localised string,
 * wrap [applicationContext] explicitly:
 *
 * ```
 * val localised = LocaleManager.wrap(applicationContext)
 * localised.getString(R.string.some_key)
 * ```
 *
 * That is the recommended pattern per
 * https://developer.android.com/guide/topics/resources/app-languages
 * (accessed February 2026).
 *
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

object SyncScheduler {
    fun enqueue(context: Context) = SyncWorker.enqueue(context)
}