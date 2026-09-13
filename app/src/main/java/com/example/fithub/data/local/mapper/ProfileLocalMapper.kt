package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.UserProfileEntity
import com.example.fithub.domain.model.UserProfile

object ProfileLocalMapper {
    fun toDomain(e: UserProfileEntity) = UserProfile(
        id = e.id,
        firstName = e.firstName,
        surname = e.surname,
        email = e.email,
        gender = e.gender,
        age = e.age,
        heightCm = e.heightCm,
        startingWeightKg = e.startingWeightKg,
        currentWeightKg = e.currentWeightKg,
        activityLevel = e.activityLevel,
        profileImageUrl = e.profileImageUrl,
        biometricEnabled = e.biometricEnabled,
        onboardingComplete = e.onboardingComplete,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt
    )

    fun toEntity(d: UserProfile) = UserProfileEntity(
        id = d.id,
        firstName = d.firstName,
        surname = d.surname,
        email = d.email,
        gender = d.gender,
        age = d.age,
        heightCm = d.heightCm,
        startingWeightKg = d.startingWeightKg,
        currentWeightKg = d.currentWeightKg,
        activityLevel = d.activityLevel,
        profileImageUrl = d.profileImageUrl,
        biometricEnabled = d.biometricEnabled,
        onboardingComplete = d.onboardingComplete,
        createdAt = d.createdAt,
        updatedAt = d.updatedAt
    )
}