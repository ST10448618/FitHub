package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    fun observeById(userId: String): Flow<UserProfileEntity?>

    @Query("UPDATE user_profile SET currentWeightKg = :weightKg, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateCurrentWeight(userId: String, weightKg: Double, updatedAt: String)

    @Query("UPDATE user_profile SET onboardingComplete = 1 WHERE id = :userId")
    suspend fun markOnboardingComplete(userId: String)

    @Query("DELETE FROM user_profile WHERE id = :userId")
    suspend fun delete(userId: String)
}