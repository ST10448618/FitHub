package com.example.fithub.core

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/**
 * Single source of truth for the current user's UID.
 * Reads directly from Firebase Auth so it always matches the real session.
 */
object SessionManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    /** Current authenticated user's UID, or null if not logged in. */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = currentUser != null

    fun logout() {
        Log.i(LogTags.AUTH, "Explicit logout requested; signing out Firebase Auth")
        auth.signOut()
        Log.i(LogTags.AUTH, "Firebase Auth signed out; currentUser=${auth.currentUser?.uid ?: "null"}")
    }

    private val currentUser get() = auth.currentUser
}