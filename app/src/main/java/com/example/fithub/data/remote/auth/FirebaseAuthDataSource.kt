package com.example.fithub.data.remote.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper over FirebaseAuth so the rest of the code never touches Firebase directly.
 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = Firebase.auth
) {

    fun currentUserId(): String? =
        auth.currentUser?.uid

    fun currentUser(): FirebaseUser? =
        auth.currentUser

    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
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

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}
