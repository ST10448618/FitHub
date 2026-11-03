package com.example.fithub.core.notifications

/**
 * Centralised channel IDs so UI, service, and workers all reference the same
 * strings. Channel display names/descriptions come from `strings.xml` (see
 * [NotificationHelper.createChannels]).
 *
 * Reference: https://developer.android.com/develop/ui/views/notifications/channels
 * (accessed February 2026)
 */
object NotificationChannels {
    /** General announcements: streaks, achievements, weekly challenge. */
    const val GENERAL = "fithub_general"
    /** Checkpoint / weigh-in reminders. */
    const val REMINDERS = "fithub_reminders"
}