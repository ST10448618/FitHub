package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.UserProfileDao
import com.example.fithub.data.local.mapper.ProfileLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.UserMapper
import com.example.fithub.domain.model.UserProfile
import com.example.fithub.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class UserRepositoryImpl(
    private val dao: UserProfileDao
) : UserRepository {

    override fun observeProfile(uid: String): Flow<UserProfile?> =
        dao.observeById(uid).map { it?.let(ProfileLocalMapper::toDomain) }

    override suspend fun getProfile(uid: String): UserProfile? =
        dao.getById(uid)?.let(ProfileLocalMapper::toDomain)

    override suspend fun createProfile(profile: UserProfile): Resource<Unit> = try {
        // Write to Room first (offline-first)
        dao.upsert(ProfileLocalMapper.toEntity(profile))

        // Push to Firestore in the background
        FirestorePaths.user(profile.id).set(UserMapper.toMap(profile)).await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save profile", e)
    }

    override suspend fun updateProfile(profile: UserProfile): Resource<Unit> = try {
        val updated = profile.copy(updatedAt = LocalDateTime.now())
        dao.upsert(ProfileLocalMapper.toEntity(updated))
        FirestorePaths.user(updated.id).set(UserMapper.toMap(updated)).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update profile", e)
    }

    override suspend fun updateCurrentWeight(uid: String, weightKg: Double): Resource<Unit> = try {
        dao.updateCurrentWeight(uid, weightKg, LocalDateTime.now().toString())
        FirestorePaths.user(uid).update("currentWeightKg", weightKg).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update weight", e)
    }

    override suspend fun markOnboardingComplete(uid: String): Resource<Unit> = try {
        dao.markOnboardingComplete(uid)
        FirestorePaths.user(uid).update("onboardingComplete", true).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to mark onboarding complete", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> {
        return try {
            val doc = FirestorePaths.user(uid).get().await()
            if (!doc.exists()) return Resource.Error("No remote profile found.")

            val map = doc.data ?: return Resource.Error("Remote profile is empty.")
            val profile = UserMapper.fromMap(uid, map)
            dao.upsert(ProfileLocalMapper.toEntity(profile))

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync profile", e)
        }
    }
}