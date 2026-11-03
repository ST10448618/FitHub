package com.example.fithub.data.repository

import android.util.Log
import com.example.fithub.core.LogTags
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
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDateTime

class UserRepositoryImpl(
    private val dao: UserProfileDao
) : UserRepository {

    override fun observeProfile(uid: String): Flow<UserProfile?> =
        dao.observeById(uid).map { it?.let(ProfileLocalMapper::toDomain) }

    override suspend fun getProfile(uid: String): UserProfile? =
        dao.getById(uid)?.let(ProfileLocalMapper::toDomain)

    /**
     * Writes Room first (immediate), then Firestore with a 15 s guard.
     * A timeout is not a failure — the local write is authoritative until a
     * later sync window. This prevents onboarding from hanging when Firestore
     * is slow or the user is on a flaky network.
     *
     * Reference: https://developer.android.com/topic/architecture/data-layer/offline-first#write-through
     * (accessed February 2026)
     */
    override suspend fun createProfile(profile: UserProfile): Resource<Unit> = try {
        dao.upsert(ProfileLocalMapper.toEntity(profile))
        val pushOk = withTimeoutOrNull(15_000L) {
            FirestorePaths.user(profile.id).set(UserMapper.toMap(profile)).await()
        }
        if (pushOk == null) {
            Log.w(LogTags.SYNC, "createProfile: Firestore push timed out — kept local")
        } else {
            Log.d(LogTags.SYNC, "createProfile pushed uid=${profile.id.take(8)}…")
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "createProfile failed", e)
        Resource.Error(e.message ?: "Failed to save profile", e)
    }

    override suspend fun updateProfile(profile: UserProfile): Resource<Unit> = try {
        val updated = profile.copy(updatedAt = LocalDateTime.now())
        dao.upsert(ProfileLocalMapper.toEntity(updated))
        FirestorePaths.user(updated.id).set(UserMapper.toMap(updated)).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "updateProfile failed", e)
        Resource.Error(e.message ?: "Failed to update profile", e)
    }

    override suspend fun updateCurrentWeight(uid: String, weightKg: Double): Resource<Unit> = try {
        dao.updateCurrentWeight(uid, weightKg, LocalDateTime.now().toString())
        FirestorePaths.user(uid).update("currentWeightKg", weightKg).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "updateCurrentWeight failed", e)
        Resource.Error(e.message ?: "Failed to update weight", e)
    }

    override suspend fun markOnboardingComplete(uid: String): Resource<Unit> = try {
        dao.markOnboardingComplete(uid)
        FirestorePaths.user(uid).update("onboardingComplete", true).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "markOnboardingComplete failed", e)
        Resource.Error(e.message ?: "Failed to mark onboarding complete", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> {
        return try {
            val doc = FirestorePaths.user(uid).get().await()
            if (!doc.exists()) return Resource.Error("No remote profile found.")
            val map = doc.data ?: return Resource.Error("Remote profile is empty.")
            val profile = UserMapper.fromMap(uid, map)
            dao.upsert(ProfileLocalMapper.toEntity(profile))
            Log.d(LogTags.SYNC, "User syncFromRemote ok uid=${uid.take(8)}…")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(LogTags.SYNC, "User syncFromRemote failed", e)
            Resource.Error(e.message ?: "Failed to sync profile", e)
        }
    }
}