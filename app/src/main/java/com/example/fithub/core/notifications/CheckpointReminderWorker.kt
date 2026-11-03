package com.example.fithub.core.notifications

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.SessionManager
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.CheckpointSchedule
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Daily worker that fires the "weigh-in due" reminder once the schedule says
 * a checkpoint is due. Only runs when a checkpoint reminder is enabled **and**
 * push notifications are on in-app.
 *
 * Scheduling policy: this worker is a **periodic daily check**. Repetition is
 * suppressed by comparing `schedule.nextCheckpointDate` to today and by only
 * posting a notification when the date has been reached (or passed).
 *
 * Reference: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
 * (accessed February 2026)
 */
class CheckpointReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = SessionManager.currentUserId
        if (uid == null) {
            Log.d(LogTags.PUSH, "CheckpointReminder: no session — success (nothing to do)")
            return Result.success()
        }
        val schedule = runCatching { ServiceLocator.checkpointRepository.get(uid) }
            .onFailure { Log.e(LogTags.PUSH, "CheckpointReminder: read failed", it) }
            .getOrNull()
        if (schedule == null) {
            Log.d(LogTags.PUSH, "CheckpointReminder: no schedule — success")
            return Result.success()
        }
        if (!schedule.remindersEnabled) {
            Log.d(LogTags.PUSH, "CheckpointReminder: reminders disabled — success")
            return Result.success()
        }
        val pushEnabled = runCatching {
            ServiceLocator.preferencesManager.pushNotificationsEnabled.first()
        }.getOrDefault(true)
        if (!pushEnabled) {
            Log.d(LogTags.PUSH, "CheckpointReminder: push disabled in-app — success")
            return Result.success()
        }
        val today = LocalDate.now()
        if (schedule.nextCheckpointDate.isAfter(today)) {
            Log.d(LogTags.PUSH, "CheckpointReminder: next=${schedule.nextCheckpointDate} future — success")
            return Result.success()
        }
        NotificationHelper.showNotification(
            context = applicationContext,
            channelId = NotificationChannels.REMINDERS,
            notificationId = NOTIFICATION_ID,
            title = applicationContext.getString(R.string.notif_checkpoint_title),
            body = applicationContext.getString(R.string.notif_checkpoint_body),
            route = com.example.fithub.ui.navigation.Screen.BODY_WEIGHT
        )
        Log.i(LogTags.PUSH, "CheckpointReminder posted for ${schedule.nextCheckpointDate}")
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "fithub_checkpoint_reminder"
        private const val NOTIFICATION_ID = 3001
        private const val INTERVAL_HOURS = 24L

        /**
         * Schedule daily when reminders are enabled. Idempotent: called on
         * every `save(...)` and on cold start. `UPDATE` replaces the existing
         * work if the constraints haven't changed, no extra worker spawns.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CheckpointReminderWorker>(
                INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.d(LogTags.PUSH, "CheckpointReminderWorker scheduled (unique=$UNIQUE_NAME)")
        }

        /** Cancel when reminders are disabled so the platform stops waking us. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
            Log.d(LogTags.PUSH, "CheckpointReminderWorker cancelled (unique=$UNIQUE_NAME)")
        }

        /**
         * Convenience: `schedule` if [scheduleConfig.remindersEnabled], else `cancel`.
         * Called from [com.example.fithub.data.repository.CheckpointRepositoryImpl.save].
         */
        fun reschedule(context: Context, scheduleConfig: CheckpointSchedule) {
            if (scheduleConfig.remindersEnabled) schedule(context) else cancel(context)
        }
    }
}