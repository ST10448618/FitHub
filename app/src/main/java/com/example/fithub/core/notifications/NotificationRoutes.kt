
package com.example.fithub.core.notifications

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.ui.navigation.Screen

/**
 * Whitelist-based parser for the `route` extra carried by notification
 * intents. Only routes the user can legitimately reach **after authentication**
 * are accepted — this stops a stale notification from deep-linking into
 * onboarding or a `session_mode/{planId}` screen with a missing argument.
 *
 * Reference: https://developer.android.com/develop/ui/views/notifications/navigation#direct-entry
 * (accessed February 2026)
 */
object NotificationRoutes {

    /** Parameterless routes that are safe to deep-link to after login. */
    val allowed: Set<String> = setOf(
        Screen.DASHBOARD,
        Screen.JOURNAL,
        Screen.PLANS,
        Screen.GOALS,
        Screen.REWARDS,
        Screen.PROGRESS_OVERVIEW,
        Screen.NUTRITION_OVERVIEW,
        Screen.WORKOUTS_OVERVIEW,
        Screen.BODY_WEIGHT,
        Screen.CHECKPOINT_MANAGER,
        Screen.CHECKPOINT_LOGGER,
        Screen.WEIGHT_GOAL,
        Screen.NUTRITION_GOALS,
        Screen.WORKOUT_GOALS,
        Screen.PROFILE,
        Screen.EDIT_PROFILE,
        Screen.SETTINGS,
        Screen.ACHIEVEMENTS,
        Screen.VERIFIED_PLANS,
        Screen.ADD_MEAL
    )

    /**
     * Returns a safe route if [raw] is exactly one of [allowed]; otherwise
     * returns `null` and logs the rejection with `FitHub/Push`.
     */
    fun sanitise(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return if (raw in allowed) {
            Log.d(LogTags.PUSH, "Notification route accepted: $raw")
            raw
        } else {
            Log.w(LogTags.PUSH, "Notification route rejected (not on whitelist): $raw")
            null
        }
    }
}