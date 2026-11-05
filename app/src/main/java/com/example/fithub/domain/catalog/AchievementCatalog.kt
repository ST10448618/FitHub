package com.example.fithub.domain.catalog

import com.example.fithub.R
import com.example.fithub.domain.model.Achievement
import com.example.fithub.domain.model.AchievementCategory

/**
 * Static definitions of every achievement in FitHub.
 * Progress state is per-user and lives in UserAchievement.
 *
 * As with `RewardCatalog`, `title`/`description` keep the English fallback
 * while `titleRes`/`descriptionRes` are the ready-made localisation hooks.
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
    // ---- Streak milestones (UDF 5) ----
    const val STREAK_3 = "streak_3"
    const val STREAK_30 = "streak_30"

    val ALL: List<Achievement> = listOf(
        // ---- Nutrition ----
        Achievement(
            id = FIRST_LOG,
            title = "First Log",
            description = "Logged your first meal.",
            category = AchievementCategory.NUTRITION,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_first_log_title,
            descriptionRes = R.string.achievement_first_log_desc
        ),
        Achievement(
            id = HIT_GOAL,
            title = "Hit Goal",
            description = "Reached your daily target.",
            category = AchievementCategory.NUTRITION,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_hit_goal_title,
            descriptionRes = R.string.achievement_hit_goal_desc
        ),
        Achievement(
            id = PERFECT_WEEK,
            title = "Perfect Week",
            description = "Hit your goals 7 days straight.",
            category = AchievementCategory.NUTRITION,
            goalValue = 7,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_perfect_week_title,
            descriptionRes = R.string.achievement_perfect_week_desc
        ),
        Achievement(
            id = MONTHLY_STREAK,
            title = "Monthly Streak",
            description = "Logged calories all month.",
            category = AchievementCategory.NUTRITION,
            goalValue = 20,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_monthly_streak_title,
            descriptionRes = R.string.achievement_monthly_streak_desc
        ),

        // ---- Workout ----
        Achievement(
            id = FIRST_SESSION,
            title = "First Session",
            description = "Completed first workout.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_first_session_title,
            descriptionRes = R.string.achievement_first_session_desc
        ),
        Achievement(
            id = WEEKLY_GOAL,
            title = "Weekly Goal",
            description = "Hit weekly session target.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_weekly_goal_title,
            descriptionRes = R.string.achievement_weekly_goal_desc
        ),
        Achievement(
            id = WORKOUT_STREAK,
            title = "Workout Streak",
            description = "Trained 4 weeks in a row.",
            category = AchievementCategory.WORKOUT,
            goalValue = 4,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_workout_streak_title,
            descriptionRes = R.string.achievement_workout_streak_desc
        ),
        Achievement(
            id = MONTHLY_MILESTONE,
            title = "Monthly Milestone",
            description = "Hit monthly activity goal.",
            category = AchievementCategory.WORKOUT,
            goalValue = 1,
            rewardParticles = 150,
            iconName = "medal",
            titleRes = R.string.achievement_monthly_milestone_title,
            descriptionRes = R.string.achievement_monthly_milestone_desc
        )
        ,
        // ---- Streak milestones (UDF 5) ----
        Achievement(
            id = STREAK_3,
            title = "3-Day Streak",
            description = "Logged activity three days in a row.",
            category = AchievementCategory.NUTRITION,
            goalValue = 3,
            rewardParticles = 100,
            iconName = "medal",
            titleRes = R.string.achievement_streak_3_title,
            descriptionRes = R.string.achievement_streak_3_desc
        ),
        Achievement(
            id = STREAK_30,
            title = "30-Day Streak",
            description = "One full month of daily activity.",
            category = AchievementCategory.NUTRITION,
            goalValue = 30,
            rewardParticles = 500,
            iconName = "medal",
            titleRes = R.string.achievement_streak_30_title,
            descriptionRes = R.string.achievement_streak_30_desc
        )
    )

    fun byId(id: String): Achievement? = ALL.firstOrNull { it.id == id }
    fun byCategory(category: AchievementCategory): List<Achievement> =
        ALL.filter { it.category == category }

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