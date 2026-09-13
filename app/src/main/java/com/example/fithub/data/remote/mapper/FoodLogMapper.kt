package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.model.FoodSource
import com.example.fithub.domain.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime

object FoodLogMapper {
    fun toMap(l: FoodLog): Map<String, Any?> = mapOf(
        "foodId" to l.foodId,
        "foodName" to l.foodName,
        "brandName" to l.brandName,
        "imageUrl" to l.imageUrl,
        "mealType" to l.mealType.name,
        "source" to l.source.name,
        "barcode" to l.barcode,
        "portionSize" to l.portionSize,
        "portionUnit" to l.portionUnit,
        "portionGrams" to l.portionGrams,
        "calories" to l.calories,
        "proteinG" to l.proteinG,
        "carbsG" to l.carbsG,
        "fatG" to l.fatG,
        "logDate" to l.logDate.toString(),
        "loggedAt" to l.loggedAt.toString()
    )

    fun fromMap(id: String, uid: String, map: Map<String, Any?>): FoodLog = FoodLog(
        id = id,
        userId = uid,
        foodId = map["foodId"] as? String,
        foodName = map["foodName"] as? String ?: "Unknown food",
        brandName = map["brandName"] as? String,
        imageUrl = map["imageUrl"] as? String,
        mealType = (map["mealType"] as? String)?.let {
            runCatching { MealType.valueOf(it) }.getOrDefault(MealType.SNACK)
        } ?: MealType.SNACK,
        source = (map["source"] as? String)?.let {
            runCatching { FoodSource.valueOf(it) }.getOrDefault(FoodSource.MANUAL)
        } ?: FoodSource.MANUAL,
        barcode = map["barcode"] as? String,
        portionSize = (map["portionSize"] as? Number)?.toDouble() ?: 1.0,
        portionUnit = map["portionUnit"] as? String ?: "serving",
        portionGrams = (map["portionGrams"] as? Number)?.toDouble() ?: 0.0,
        calories = (map["calories"] as? Number)?.toDouble() ?: 0.0,
        proteinG = (map["proteinG"] as? Number)?.toDouble() ?: 0.0,
        carbsG = (map["carbsG"] as? Number)?.toDouble() ?: 0.0,
        fatG = (map["fatG"] as? Number)?.toDouble() ?: 0.0,
        logDate = (map["logDate"] as? String)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        } ?: LocalDate.now(),
        loggedAt = (map["loggedAt"] as? String)?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        } ?: LocalDateTime.now()
    )
}