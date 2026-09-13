package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.NutritionGoalsEntity
import com.example.fithub.domain.model.MacroTargets
import com.example.fithub.domain.model.MealTargets
import com.example.fithub.domain.model.NutritionGoals

object NutritionGoalsLocalMapper {
    fun toDomain(e: NutritionGoalsEntity) = NutritionGoals(
        id = e.id,
        userId = e.userId,
        recommendedDailyCalories = e.recommendedDailyCalories,
        userDailyCalories = e.userDailyCalories,
        mealTargets = MealTargets(e.breakfastKcal, e.lunchKcal, e.dinnerKcal, e.snackKcal),
        macroTargets = MacroTargets(e.proteinG, e.carbsG, e.fatG),
        effectiveDate = e.effectiveDate,
        updatedAt = e.updatedAt
    )

    fun toEntity(d: NutritionGoals) = NutritionGoalsEntity(
        id = d.id,
        userId = d.userId,
        recommendedDailyCalories = d.recommendedDailyCalories,
        userDailyCalories = d.userDailyCalories,
        breakfastKcal = d.mealTargets.breakfastKcal,
        lunchKcal = d.mealTargets.lunchKcal,
        dinnerKcal = d.mealTargets.dinnerKcal,
        snackKcal = d.mealTargets.snackKcal,
        proteinG = d.macroTargets.proteinG,
        carbsG = d.macroTargets.carbsG,
        fatG = d.macroTargets.fatG,
        effectiveDate = d.effectiveDate,
        updatedAt = d.updatedAt
    )
}