package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender
import java.time.LocalDateTime

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val surname: String,
    val email: String,
    val gender: Gender,
    val age: Int,
    val heightCm: Double,
    val startingWeightKg: Double,
    val currentWeightKg: Double,
    val activityLevel: ActivityLevel,
    val profileImageUrl: String?,
    val biometricEnabled: Boolean,
    val onboardingComplete: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
