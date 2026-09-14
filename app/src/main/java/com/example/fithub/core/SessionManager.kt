package com.example.fithub.core

/**
 * Holds the current user ID after authentication.
 * Written by LoginViewModel and OnboardingViewModel.
 * Read by every ViewModel that needs the current user's data.
 */
object SessionManager {

    private var _currentUserId: String? = null

    val currentUserId: String? get() = _currentUserId

    fun setUser(uid: String?) {
        _currentUserId = uid
    }

    fun currentUserIdOrThrow(): String =
        _currentUserId ?: error("No user session — call setUser() after login/onboarding.")

    fun isLoggedIn(): Boolean = _currentUserId != null


    fun requireUserId(): String =
        _currentUserId ?: error("No user session — call setUser() after login/onboarding.")
}