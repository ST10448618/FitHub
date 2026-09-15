package com.example.fithub.core

import com.google.firebase.auth.FirebaseAuth

/**
 * Single source of truth for the current user's UID.
 * Reads directly from Firebase Auth so it always matches the real session.
 *
 * DO NOT hard-code a demo UID here — screens will silently query for the
 * wrong user and appear empty or corrupt.
 */
object SessionManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    /** Current authenticated user's UID, or null if not logged in. */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = currentUser != null

    fun logout() {
        auth.signOut()
    }


    private val currentUser get() = auth.currentUser
}