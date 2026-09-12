package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.FoodSource
import java.time.LocalDateTime

@Entity(tableName = "food")
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val category: String,
    val caloriesPer100g: Double?,
    val proteinPer100g: Double?,
    val carbsPer100g: Double?,
    val fatPer100g: Double?,
    val fiberPer100g: Double?,
    val sugarPer100g: Double?,
    val sodiumPer100g: Double?,
    val servingSizeG: Double?,
    val servingLabel: String?,
    val source: FoodSource,
    val description: String?,
    val cachedAt: LocalDateTime
)