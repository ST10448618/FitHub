package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A logged food entry.
 * All nutrition values are snapshotted at log time so that future API changes
 * do not rewrite history.
 */
data class FoodLog(
    val id: String,
    val userId: String,
    val foodId: String? = null,              // reference to Food, may be null for manual
    val foodName: String,                    // snapshot
    val brandName: String? = null,           // snapshot
    val imageUrl: String? = null,            // snapshot
    val mealType: MealType,
    val source: FoodSource,
    val barcode: String? = null,

    val portionSize: Double,                 // e.g. 2.0
    val portionUnit: String,                 // "serving", "g", "ml", etc.
    val portionGrams: Double,                // normalized grams (used for calc)

    val calories: Double,                    // snapshot for this log
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,

    val logDate: LocalDate,                  // for grouping by day
    val loggedAt: LocalDateTime = LocalDateTime.now()
)