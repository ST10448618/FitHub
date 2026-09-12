package com.example.fithub.domain.model

import java.time.LocalDateTime

/**
 * A static definition of an achievement.
 * The per-user progress state lives in UserAchievement.
 */
data class Achievement(
    val id: String,                         // e.g. "first_log"
    val title: String,                      // "First Log"
    val description: String,
    val category: AchievementCategory,
    val goalValue: Int,                     // e.g. 1 log, 7 days streak
    val rewardParticles: Int,               // e.g. 150
    val iconName: String = "default"
)

/**
 * Per-user progress toward an achievement.
 */
data class UserAchievement(
    val achievementId: String,
    val userId: String,
    val progress: Int = 0,                  // current value
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val claimedAt: LocalDateTime? = null
)