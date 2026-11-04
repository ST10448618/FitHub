package com.example.fithub.domain.model

import androidx.annotation.StringRes
import java.time.LocalDateTime

/**
 * A static definition of an achievement.
 * The per-user progress state lives in UserAchievement.
 *
 * `title`/`description` remain the canonical English strings — any sync path
 * that needs a stable identifier should use `id`, not the display text. The
 * `titleRes`/`descriptionRes` fields default to `0` so existing code that
 * constructs `Achievement(...)` without them keeps compiling; provide them
 * whenever a caller wants the localised version.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val goalValue: Int,
    val rewardParticles: Int,
    val iconName: String = "default",
    @param:StringRes val titleRes: Int = 0,
    @param:StringRes val descriptionRes: Int = 0
)

/**
 * Per-user progress toward an achievement.
 */
data class UserAchievement(
    val achievementId: String,
    val userId: String,
    val progress: Int = 0,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val claimedAt: LocalDateTime? = null
)