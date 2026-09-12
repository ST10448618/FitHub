package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "nutrition_goals", indices = [Index("userId")])
data class NutritionGoalsEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val recommendedDailyCalories: Int,
    val userDailyCalories: Int,

    // Meal targets (flattened MealTargets)
    val breakfastKcal: Int,
    val lunchKcal: Int,
    val dinnerKcal: Int,
    val snackKcal: Int,

    // Macro targets (flattened MacroTargets)
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,

    val effectiveDate: LocalDate,
    val updatedAt: LocalDateTime
)