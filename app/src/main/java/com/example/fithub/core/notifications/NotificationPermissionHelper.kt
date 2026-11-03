package com.example.fithub.core.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.fithub.core.LogTags

/**
 * Runtime-permission helper for `POST_NOTIFICATIONS` (Android 13+ / API 33+).
 *
 * On API < 33 notifications are implicitly allowed and this class reduces to a
 * capability probe (`NotificationManagerCompat.areNotificationsEnabled()`).
 *
 * The Compose side uses `rememberLauncherForActivityResult(RequestPermission())`
 * to ask; this helper just answers **"what should the UI show?"** — either a
 * request prompt or a "go to system settings" fallback.
 *
 * References:
 *  • https://developer.android.com/develop/ui/views/notifications/notification-permission
 *  • https://developer.android.com/training/permissions/requesting
 * (accessed February 2026)
 */
object NotificationPermissionHelper {

    /** Does the platform allow notifications right now? */
    fun isGranted(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** True if we still need to *ask* the user (API 33+, not yet granted). */
    fun requiresRuntimeRequest(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted(context)

    /**
     * True when the platform will no longer show the system dialog — either
     * the user tapped "Don't allow" twice on API 33+, or notifications were
     * turned off in system settings. In that case the only route forward is
     * the app's notification settings page.
     */
    fun shouldRouteToSettings(context: Context, rationaleWasShown: Boolean): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isGranted(context) &&
                rationaleWasShown

    /**
     * Opens the app's notification settings page (`Settings.ACTION_APP_NOTIFICATION_SETTINGS`)
     * which is the correct target on API 26+; falls back to the generic app
     * details screen for very old devices.
     *
     * Reference: https://developer.android.com/reference/android/provider/Settings#ACTION_APP_NOTIFICATION_SETTINGS
     * (accessed February 2026)
     */
    fun openAppNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        }
        runCatching {
            Log.i(LogTags.PUSH, "Opening notification settings for ${context.packageName}")
            context.startActivity(intent)
        }.onFailure {
            Log.e(LogTags.PUSH, "Failed to open notification settings", it)
        }
    }
}