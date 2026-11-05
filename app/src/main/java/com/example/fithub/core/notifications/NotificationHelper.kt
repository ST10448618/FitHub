package com.example.fithub.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fithub.MainActivity
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.ui.navigation.Screen

/**
 * Factory for every user-visible notification.
 *
 * References:
 *  • https://developer.android.com/develop/ui/views/notifications/build-notification
 *  • https://developer.android.com/develop/ui/views/notifications/channels
 *  • https://developer.android.com/develop/ui/views/notifications/navigation
 * (accessed February 2026)
 */
object NotificationHelper {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.GENERAL,
                context.getString(R.string.notif_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_general_desc)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                context.getString(R.string.notif_channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_reminders_desc)
            }
        )
        Log.i(LogTags.PUSH, "Notification channels registered (general + reminders)")
    }

    fun showNotification(
        context: Context,
        channelId: String = NotificationChannels.GENERAL,
        notificationId: Int,
        title: String,
        body: String,
        route: String? = null
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(LogTags.PUSH, "showNotification skipped — notifications disabled by user")
            return
        }
        val safeRoute = NotificationRoutes.sanitise(route)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (safeRoute != null) putExtra(EXTRA_ROUTE, safeRoute)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_fithub)
            .setColor(context.getColor(R.color.fithub_notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(LogTags.PUSH, "Notification posted channel=$channelId id=$notificationId route=${safeRoute ?: "null"}")
        } catch (t: SecurityException) {
            Log.e(LogTags.PUSH, "Notification post failed — permission revoked", t)
        }
    }

    /**
     * Phase 6 (UDF 5): fires the streak-milestone congratulation notification.
     * Respects the in-app push preference; the caller is responsible for
     * having created the channels in `FitHubApplication.onCreate`.
     */
    fun showStreakMilestone(context: Context, days: Int) {
        if (days <= 0) return
        showNotification(
            context = context,
            channelId = NotificationChannels.GENERAL,
            notificationId = 9000 + days,
            title = context.getString(R.string.streak_milestone_notification_title),
            body = context.getString(R.string.streak_milestone_notification_body, days),
            route = Screen.DASHBOARD
        )
        Log.i(LogTags.STREAK, "Milestone notification fired for $days days")
    }

    const val EXTRA_ROUTE: String = "com.example.fithub.extra.ROUTE"
}