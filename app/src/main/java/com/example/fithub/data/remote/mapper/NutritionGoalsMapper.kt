package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.MacroTargets
import com.example.fithub.domain.model.MealTargets
import com.example.fithub.domain.model.NutritionGoals
import java.time.LocalDate
import java.time.LocalDateTime

object NutritionGoalsMapper {
    fun toMap(g: NutritionGoals): Map<String, Any?> = mapOf(
        "recommendedDailyCalories" to g.recommendedDailyCalories,
        "userDailyCalories" to g.userDailyCalories,
        "breakfastKcal" to g.mealTargets.breakfastKcal,
        "lunchKcal" to g.mealTargets.lunchKcal,
        "dinnerKcal" to g.mealTargets.dinnerKcal,
        "snackKcal" to g.mealTargets.snackKcal,
        "proteinG" to g.macroTargets.proteinG,
        "carbsG" to g.macroTargets.carbsG,
        "fatG" to g.macroTargets.fatG,
        "effectiveDate" to g.effectiveDate.toString(),
        "updatedAt" to g.updatedAt.toString()
    )

    fun fromMap(id: String, uid: String, map: Map<String, Any?>): NutritionGoals =
        NutritionGoals(
            id = id,
            userId = uid,
            recommendedDailyCalories = (map["recommendedDailyCalories"] as? Number)?.toInt() ?: 0,
            userDailyCalories = (map["userDailyCalories"] as? Number)?.toInt() ?: 0,
            mealTargets = MealTargets(
                breakfastKcal = (map["breakfastKcal"] as? Number)?.toInt() ?: 0,
                lunchKcal = (map["lunchKcal"] as? Number)?.toInt() ?: 0,
                dinnerKcal = (map["dinnerKcal"] as? Number)?.toInt() ?: 0,
                snackKcal = (map["snackKcal"] as? Number)?.toInt() ?: 0
            ),
            macroTargets = MacroTargets(
                proteinG = (map["proteinG"] as? Number)?.toInt() ?: 0,
                carbsG = (map["carbsG"] as? Number)?.toInt() ?: 0,
                fatG = (map["fatG"] as? Number)?.toInt() ?: 0
            ),
            effectiveDate = (map["effectiveDate"] as? String)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now(),
            updatedAt = (map["updatedAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now()
        )
}