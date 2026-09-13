package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(uid: String): Flow<UserProfile?>
    suspend fun getProfile(uid: String): UserProfile?
    suspend fun createProfile(profile: UserProfile): Resource<Unit>
    suspend fun updateProfile(profile: UserProfile): Resource<Unit>
    suspend fun updateCurrentWeight(uid: String, weightKg: Double): Resource<Unit>
    suspend fun markOnboardingComplete(uid: String): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}