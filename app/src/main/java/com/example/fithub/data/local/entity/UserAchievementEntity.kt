package com.example.fithub.data.local.entity

import androidx.room.Entity
import java.time.LocalDateTime

@Entity(
    tableName = "user_achievement",
    primaryKeys = ["userId", "achievementId"]
)
data class UserAchievementEntity(
    val userId: String,
    val achievementId: String,
    val progress: Int,
    val isUnlocked: Boolean,
    val isClaimed: Boolean,
    val unlockedAt: LocalDateTime?,
    val claimedAt: LocalDateTime?
)