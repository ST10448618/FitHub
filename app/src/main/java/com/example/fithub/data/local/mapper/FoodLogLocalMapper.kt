package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.FoodLogEntity
import com.example.fithub.domain.model.FoodLog

object FoodLogLocalMapper {
    fun toDomain(e: FoodLogEntity) = FoodLog(
        id = e.id,
        userId = e.userId,
        foodId = e.foodId,
        foodName = e.foodName,
        brandName = e.brandName,
        imageUrl = e.imageUrl,
        mealType = e.mealType,
        source = e.source,
        barcode = e.barcode,
        portionSize = e.portionSize,
        portionUnit = e.portionUnit,
        portionGrams = e.portionGrams,
        calories = e.calories,
        proteinG = e.proteinG,
        carbsG = e.carbsG,
        fatG = e.fatG,
        logDate = e.logDate,
        loggedAt = e.loggedAt
    )

    fun toEntity(d: FoodLog) = FoodLogEntity(
        id = d.id,
        userId = d.userId,
        foodId = d.foodId,
        foodName = d.foodName,
        brandName = d.brandName,
        imageUrl = d.imageUrl,
        mealType = d.mealType,
        source = d.source,
        barcode = d.barcode,
        portionSize = d.portionSize,
        portionUnit = d.portionUnit,
        portionGrams = d.portionGrams,
        calories = d.calories,
        proteinG = d.proteinG,
        carbsG = d.carbsG,
        fatG = d.fatG,
        logDate = d.logDate,
        loggedAt = d.loggedAt
    )
}