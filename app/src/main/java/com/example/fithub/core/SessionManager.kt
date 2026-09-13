package com.example.fithub.core

/**
 * TEMPORARY user session holder.
 * Track A will replace this with real Firebase auth state.
 * For now it returns a demo UID so screens have data to display.
 */
object SessionManager {

    // TODO(Track A): replace with real auth-state collection
    private const val DEMO_UID = "demo_user"

    private var _currentUserId: String? = DEMO_UID

    val currentUserId: String? get() = _currentUserId

    fun setUser(uid: String?) {
        _currentUserId = uid
    }

    fun isLoggedIn(): Boolean = _currentUserId != null
}