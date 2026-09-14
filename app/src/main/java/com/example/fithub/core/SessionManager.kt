package com.example.fithub.core

/**
 * Temporary user session holder.
 * Track A will replace this with real Firebase auth state observation.
 */
object SessionManager {

    private var _currentUserId: String? = null

    val currentUserId: String? get() = _currentUserId

    fun setUser(uid: String?) {
        _currentUserId = uid
    }

    fun isLoggedIn(): Boolean = _currentUserId != null
}