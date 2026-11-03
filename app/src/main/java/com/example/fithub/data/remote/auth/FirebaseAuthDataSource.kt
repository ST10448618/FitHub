package com.example.fithub.data.remote.auth

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.await
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Thin wrapper over FirebaseAuth so the rest of the code never touches Firebase directly.
 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = Firebase.auth
) {

    /**
     * Result of a Google sign-in attempt.
     * @param user the authenticated Firebase user.
     * @param isNewUser true if Firebase created the account during this call
     *   (i.e. `result.additionalUserInfo.isNewUser`).
     */
    data class GoogleSignInOutcome(val user: FirebaseUser, val isNewUser: Boolean)

    fun currentUserId(): String? =
        auth.currentUser?.uid

    fun currentUser(): FirebaseUser? =
        auth.currentUser

    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun createUser(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()
        return result.user
    }

    suspend fun signIn(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = auth
            .signInWithEmailAndPassword(email, password)
            .await()
        return result.user
    }

    /**
     * Exchanges a Google ID token for a Firebase user.
     *
     * Reference: https://firebase.google.com/docs/auth/android/google-signin
     * (accessed February 2026)
     */
    suspend fun signInWithGoogleCredential(idToken: String): GoogleSignInOutcome? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: return null
        val isNewUser = result.additionalUserInfo?.isNewUser ?: false
        Log.i(
            LogTags.AUTH,
            "Firebase Google sign-in ok uid=${user.uid.take(8)}… isNewUser=$isNewUser"
        )
        return GoogleSignInOutcome(user, isNewUser)
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}