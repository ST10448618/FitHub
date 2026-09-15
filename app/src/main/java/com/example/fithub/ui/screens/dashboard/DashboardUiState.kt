package com.example.fithub.ui.screens.dashboard

import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.model.*
import java.time.LocalDate

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Greeting + profile
    val profile: UserProfile? = null,

    // Weight/goal
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,

    // Nutrition
    val nutritionSnapshot: NutritionProgressCalculator.DashboardNutrition? = null,

    // Workouts
    val weeklyWorkoutProgressPercent: Int = 0,
    val weeklyWorkoutCompleted: Int = 0,
    val weeklyWorkoutTarget: Int = 4,

    // Recent/recommended
    val recentSessions: List<WorkoutSession> = emptyList(),
    val recommendedPlans: List<WorkoutPlan> = emptyList(),

    val weeklyCalories: List<DailyCalorieBar> = emptyList(),
    val dailyCalorieTarget: Int = 0
)

data class DailyCalorieBar(
    val date: LocalDate,
    val dayLabel: String,
    val calories: Int,
    val isOverTarget: Boolean
)

