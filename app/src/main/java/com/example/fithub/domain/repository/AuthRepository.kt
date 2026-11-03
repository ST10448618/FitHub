package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import kotlinx.coroutines.flow.Flow

/**
 * The outcome of a Google sign-in exchange (Phase 2).
 * `isNewUser` = `true` when Firebase Auth reports the account was created
 * as part of this sign-in; the caller should send the user through onboarding.
 */
data class GoogleAuthResult(
    val uid: String,
    val email: String,
    val displayName: String,
    val isNewUser: Boolean
)

/**
 * Authentication abstraction. Firebase impl lives in the data layer.
 */
interface AuthRepository {
    /** Currently authenticated user ID or null. */
    fun currentUserId(): String?

    /** Observe authentication state — emits uid (or null when logged out). */
    fun observeAuthState(): Flow<String?>

    suspend fun register(email: String, password: String): Resource<String>

    suspend fun login(email: String, password: String): Resource<String>

    suspend fun logout(): Resource<Unit>

    suspend fun resetPassword(email: String): Resource<Unit>

    /**
     * Exchanges a Google ID token (from Credential Manager) for a Firebase
     * session. Returns the resulting uid + whether the Firebase user was
     * created as part of this call.
     *
     * Reference: https://firebase.google.com/docs/auth/android/google-signin#authenticate_with_firebase
     * (accessed February 2026)
     */
    suspend fun signInWithGoogle(idToken: String): Resource<GoogleAuthResult>
}