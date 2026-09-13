package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender
import com.example.fithub.domain.model.UserProfile
import java.time.LocalDateTime

object UserMapper {

    fun toMap(p: UserProfile): Map<String, Any?> = mapOf(
        "firstName" to p.firstName,
        "surname" to p.surname,
        "email" to p.email,
        "gender" to p.gender.name,
        "age" to p.age,
        "heightCm" to p.heightCm,
        "startingWeightKg" to p.startingWeightKg,
        "currentWeightKg" to p.currentWeightKg,
        "activityLevel" to p.activityLevel.name,
        "profileImageUrl" to p.profileImageUrl,
        "biometricEnabled" to p.biometricEnabled,
        "onboardingComplete" to p.onboardingComplete,
        "createdAt" to p.createdAt.toString(),
        "updatedAt" to p.updatedAt.toString()
    )

    @Suppress("UNCHECKED_CAST")
    fun fromMap(uid: String, map: Map<String, Any?>): UserProfile = UserProfile(
        id = uid,
        firstName = map["firstName"] as? String ?: "",
        surname = map["surname"] as? String ?: "",
        email = map["email"] as? String ?: "",
        gender = (map["gender"] as? String)?.let {
            runCatching { Gender.valueOf(it) }.getOrDefault(Gender.OTHER)
        } ?: Gender.OTHER,
        age = (map["age"] as? Number)?.toInt() ?: 0,
        heightCm = (map["heightCm"] as? Number)?.toDouble() ?: 0.0,
        startingWeightKg = (map["startingWeightKg"] as? Number)?.toDouble() ?: 0.0,
        currentWeightKg = (map["currentWeightKg"] as? Number)?.toDouble() ?: 0.0,
        activityLevel = (map["activityLevel"] as? String)?.let {
            runCatching { ActivityLevel.valueOf(it) }.getOrDefault(ActivityLevel.SEDENTARY)
        } ?: ActivityLevel.SEDENTARY,
        profileImageUrl = map["profileImageUrl"] as? String,
        biometricEnabled = map["biometricEnabled"] as? Boolean ?: false,
        onboardingComplete = map["onboardingComplete"] as? Boolean ?: false,
        createdAt = (map["createdAt"] as? String)?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        } ?: LocalDateTime.now(),
        updatedAt = (map["updatedAt"] as? String)?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        } ?: LocalDateTime.now()
    )
}