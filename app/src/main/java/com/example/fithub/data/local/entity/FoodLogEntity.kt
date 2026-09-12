package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.FoodSource
import com.example.fithub.domain.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "food_log",
    indices = [Index("userId"), Index("logDate"), Index("mealType")]
)
data class FoodLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val foodId: String?,
    val foodName: String,
    val brandName: String?,
    val imageUrl: String?,
    val mealType: MealType,
    val source: FoodSource,
    val barcode: String?,
    val portionSize: Double,
    val portionUnit: String,
    val portionGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val logDate: LocalDate,
    val loggedAt: LocalDateTime
)