package com.example.fithub.ui.screens.dashboard

import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.model.*
import java.time.LocalDate
import com.example.fithub.domain.model.WeeklyInsights

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: UserProfile? = null,
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val nutritionSnapshot: NutritionProgressCalculator.DashboardNutrition? = null,
    val weeklyWorkoutProgressPercent: Int = 0,
    val weeklyWorkoutCompleted: Int = 0,
    val weeklyWorkoutTarget: Int = 4,
    val recentSessions: List<WorkoutSession> = emptyList(),
    val recommendedPlans: List<WorkoutPlan> = emptyList(),
    val weeklyCalories: List<DailyCalorieBar> = emptyList(),
    val dailyCalorieTarget: Int = 0,

    // ---------- Phase 6 (UDF 5): streaks + weekly challenge ----------
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayDone: Boolean = false,
    val streakAtRisk: Boolean = false,
    val challengeCompleted: Int = 0,
    val challengeTarget: Int = 0,
    val insights: WeeklyInsights? = null,
    val insightsLoading: Boolean = false
)

/**
 * A single weekly calorie bar.
 *
 * The visible day label ("Mon", "Tue", …) is NOT stored here — it is computed
 * by `CalorieIntakeChartSection` via `DateTimeFormatter.ofLocalizedDate(...)`
 * so it renders in the user's language.
 */
data class DailyCalorieBar(
    val date: LocalDate,
    val calories: Int,
    val isOverTarget: Boolean
)
