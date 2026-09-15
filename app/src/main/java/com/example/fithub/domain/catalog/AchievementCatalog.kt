package com.example.fithub.domain.catalog

import com.example.fithub.domain.model.Achievement
import com.example.fithub.domain.model.AchievementCategory

/**
 * Static definitions of every achievement in FitHub.
 * Progress state is per-user and lives in UserAchievement.
 */
object AchievementCatalog {

    const val FIRST_LOG = "first_log"
    const val HIT_GOAL = "hit_goal"
    const val PERFECT_WEEK = "perfect_week"
    const val MONTHLY_STREAK = "monthly_streak"

    const val FIRST_SESSION = "first_session"
    const val WEEKLY_GOAL = "weekly_goal"
    const val WORKOUT_STREAK = "workout_streak"
    const val MONTHLY_MILESTONE = "monthly_milestone"

    val ALL: List<Achievement> = listOf(
        // ---- Nutrition ----
        Achievement(
            id = FIRST_LOG,
            title = "First Log",
            description = "Logged your first meal.",
            category = AchievementCategory.NUTRITION,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = HIT_GOAL,
            title = "Hit Goal",
            description = "Reached your daily target.",
            category = AchievementCategory.NUTRITION,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = PERFECT_WEEK,
            title = "Perfect Week",
            description = "Hit your goals 7 days straight.",
            category = AchievementCategory.NUTRITION,
            goalValue = 7,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = MONTHLY_STREAK,
            title = "Monthly Streak",
            description = "Logged calories all month.",
            category = AchievementCategory.NUTRITION,
            goalValue = 20,
            rewardParticles = 150,
            iconName = "medal"
        ),

        // ---- Workout ----
        Achievement(
            id = FIRST_SESSION,
            title = "First Session",
            description = "Completed first workout.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = WEEKLY_GOAL,
            title = "Weekly Goal",
            description = "Hit weekly session target.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = WORKOUT_STREAK,
            title = "Workout Streak",
            description = "Trained 4 weeks in a row.",
            category = AchievementCategory.WORKOUT,
            goalValue = 4,
            rewardParticles = 150,
            iconName = "medal"
        ),
        Achievement(
            id = MONTHLY_MILESTONE,
            title = "Monthly Milestone",
            description = "Hit monthly activity goal.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal"
        )
    )

    fun byId(id: String): Achievement? = ALL.firstOrNull { it.id == id }
    fun byCategory(category: AchievementCategory): List<Achievement> =
        ALL.filter { it.category == category }

    // ---- Level formula ----
    // Every 300 lifetime particles = 1 level. Starting at Level 1.
    const val PARTICLES_PER_LEVEL = 300

    fun levelFromLifetimeParticles(lifetimeEarned: Int): Int =
        (lifetimeEarned / PARTICLES_PER_LEVEL) + 1

    fun particlesToNextLevel(lifetimeEarned: Int): Int {
        val next = (levelFromLifetimeParticles(lifetimeEarned)) * PARTICLES_PER_LEVEL
        return (next - lifetimeEarned).coerceAtLeast(0)
    }

    fun progressWithinLevel(lifetimeEarned: Int): Float {
        val inLevel = lifetimeEarned % PARTICLES_PER_LEVEL
        return inLevel.toFloat() / PARTICLES_PER_LEVEL
    }
}