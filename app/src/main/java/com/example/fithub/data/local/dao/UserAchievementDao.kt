package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ua: UserAchievementEntity)

    @Query("SELECT * FROM user_achievement WHERE userId = :userId")
    fun observeAll(userId: String): Flow<List<UserAchievementEntity>>

    @Query("SELECT * FROM user_achievement WHERE userId = :userId AND achievementId = :achievementId LIMIT 1")
    suspend fun get(userId: String, achievementId: String): UserAchievementEntity?

    @Query("SELECT * FROM user_achievement WHERE userId = :userId AND isUnlocked = 1 AND isClaimed = 0")
    suspend fun getClaimable(userId: String): List<UserAchievementEntity>
}