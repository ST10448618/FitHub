
package com.example.fithub.core.notifications

import android.util.Log
import com.example.fithub.core.LogTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hand-off buffer for deep links opened from notifications.
 *
 * Sequence:
 *  1. `MainActivity.onCreate/onNewIntent` extracts the `route` extra, calls
 *     [NotificationRoutes.sanitise] and pushes it here.
 *  2. `FitHubNavGraph` observes [route]; once the current destination is one of
 *     the post-auth routes (`DASHBOARD`, `BIOMETRIC_GATE` success target, etc.)
 *     it calls [consume] and navigates.
 *
 * A plain `StateFlow` is enough — there is at most one notification tap
 * pending at a time and the consumer is Compose.
 */
object PendingRoute {

    private val _route = MutableStateFlow<String?>(null)
    val route: StateFlow<String?> = _route.asStateFlow()

    fun set(raw: String?) {
        val safe = NotificationRoutes.sanitise(raw) ?: return
        Log.i(LogTags.PUSH, "PendingRoute set → $safe")
        _route.value = safe
    }

    fun consume(): String? {
        val current = _route.value
        if (current != null) {
            Log.d(LogTags.PUSH, "PendingRoute consumed → $current")
            _route.value = null
        }
        return current
    }

    fun clear() {
        _route.value = null
    }
}