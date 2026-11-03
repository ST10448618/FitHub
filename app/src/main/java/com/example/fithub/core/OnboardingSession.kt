package com.example.fithub.core

import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender

object OnboardingSession {

    var username: String = ""
    var email: String = ""
    var password: String = ""
    var biometricEnabled: Boolean = false
    var age: Int? = null
    var gender: Gender? = null
    var heightCm: Double? = null
    var weightKg: Double? = null
    var activityLevel: ActivityLevel? = null

    /** Phase 2: true when the session was started via Google Sign-In. */
    var isGoogleUser: Boolean = false
    /** Phase 2: the Firebase UID from the Google sign-in (not the email). */
    var googleUid: String? = null
    /** Phase 2: display name from Google, used to pre-fill username. */
    var googleDisplayName: String? = null

    fun reset() {
        username = ""
        email = ""
        password = ""
        biometricEnabled = false
        age = null
        gender = null
        heightCm = null
        weightKg = null
        activityLevel = null
        isGoogleUser = false
        googleUid = null
        googleDisplayName = null
    }

    fun isReadyToSubmit(): Boolean {
        val passwordOk = if (isGoogleUser) true else password.length >= 6
        return username.isNotBlank() &&
                email.isNotBlank() &&
                passwordOk &&
                age != null &&
                gender != null &&
                heightCm != null &&
                weightKg != null &&
                activityLevel != null
    }
}