package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Nutrition targets.
 * `recommendedDailyCalories` comes from the CalorieEngine and is read-only.
 * `userDailyCalories` is what the user has chosen and may differ.
 * MealTargets must sum to `userDailyCalories`.
 * MacroTargets are user-editable tracking goals, not prescriptions.
 */
data class NutritionGoals(
    val id: String,
    val userId: String,
    val recommendedDailyCalories: Int,
    val userDailyCalories: Int,
    val mealTargets: MealTargets,
    val macroTargets: MacroTargets,
    val effectiveDate: LocalDate = LocalDate.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class MealTargets(
    val breakfastKcal: Int = 0,
    val lunchKcal: Int = 0,
    val dinnerKcal: Int = 0,
    val snackKcal: Int = 0
) {
    val total: Int get() = breakfastKcal + lunchKcal + dinnerKcal + snackKcal
}

data class MacroTargets(
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0
)