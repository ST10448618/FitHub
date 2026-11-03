// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fithub.MainActivity
import com.example.fithub.R
import com.example.fithub.core.LogTags

/**
 * Factory for every user-visible notification.
 *
 * Responsibilities:
 *  • idempotently create the two channels ([NotificationChannels.GENERAL],
 *    [NotificationChannels.REMINDERS]) at app start;
 *  • build a valid `NotificationCompat.Builder` with an immutable
 *    `PendingIntent` pointing at [MainActivity] and carrying an optional
 *    `route` extra for deep linking;
 *  • degrade gracefully when the user has revoked notification permission.
 *
 * References:
 *  • https://developer.android.com/develop/ui/views/notifications/build-notification
 *  • https://developer.android.com/develop/ui/views/notifications/channels
 *  • https://developer.android.com/develop/ui/views/notifications/navigation
 * (accessed February 2026)
 */
object NotificationHelper {

    /** Called from `FitHubApplication.onCreate`. Safe to call repeatedly. */
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

    /**
     * Posts a notification through [NotificationManagerCompat].
     *
     * @param route optional deep-link target; sanitised before it is put in the
     *   intent so only whitelisted destinations can be reached from a push.
     */
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
            /* requestCode = */ notificationId, // unique per notification → no reuse
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
            NotificationManagerCompat.from(context)
                .notify(notificationId, notification)
            Log.i(
                LogTags.PUSH,
                "Notification posted channel=$channelId id=$notificationId route=${safeRoute ?: "null"}"
            )
        } catch (t: SecurityException) {
            // POST_NOTIFICATIONS was revoked between the check above and now.
            Log.e(LogTags.PUSH, "Notification post failed — permission revoked", t)
        }
    }

    /** Extra key used by [MainActivity] to read the deep-link route. */
    const val EXTRA_ROUTE: String = "com.example.fithub.extra.ROUTE"

    /** Optional test accent colour (unchanged in production). */
    @Suppress("unused")
    private const val ACCENT_HEX = "#212C99"
}