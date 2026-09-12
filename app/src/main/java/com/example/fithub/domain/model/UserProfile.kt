package com.example.fithub.domain.model

import java.time.LocalDateTime

/**
 * The single authoritative user profile.
 * `currentWeightKg` is updated by the Checkpoint system, not by Profile edit.
 * `startingWeightKg` is set during onboarding and never changes.
 */
data class UserProfile(
    val id: String,                     // Firebase Auth UID
    val firstName: String = "",
    val surname: String = "",
    val email: String = "",
    val gender: Gender = Gender.OTHER,
    val age: Int = 0,
    val heightCm: Double = 0.0,
    val startingWeightKg: Double = 0.0,
    val currentWeightKg: Double = 0.0,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val profileImageUrl: String? = null,
    val biometricEnabled: Boolean = false,
    val onboardingComplete: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)