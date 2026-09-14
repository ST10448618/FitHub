package com.example.fithub.core

import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender

/**
 * In-memory onboarding state.
 * Persists until submission; cleared after profile is written to Firebase + Room.
 * Track A owns this — replace with a graph-scoped ViewModel later if desired.
 */
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
    }

    fun isReadyToSubmit(): Boolean =
        username.isNotBlank() &&
                email.isNotBlank() &&
                password.length >= 6 &&
                age != null &&
                gender != null &&
                heightCm != null &&
                weightKg != null &&
                activityLevel != null
}