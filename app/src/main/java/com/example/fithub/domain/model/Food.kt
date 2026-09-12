package com.example.fithub.domain.model

/**
 * A food/product from OpenFoodFacts or a manual entry.
 * Nutrition is stored per 100g (typical OpenFoodFacts layout).
 * `servingSizeG` allows the app to compute per-serving values.
 *
 * Every optional field can be null — OpenFoodFacts is community-contributed.
 */
data class Food(
    val id: String,                          // barcode for OFF foods, UUID for manual
    val name: String,
    val brand: String? = null,
    val imageUrl: String? = null,
    val category: String = "Other",          // FitHub broad category

    // Nutrition per 100g
    val caloriesPer100g: Double? = null,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val fiberPer100g: Double? = null,
    val sugarPer100g: Double? = null,
    val sodiumPer100g: Double? = null,

    // Serving info
    val servingSizeG: Double? = null,
    val servingLabel: String? = null,        // e.g. "1 cup", "1 medium"

    val source: FoodSource = FoodSource.SEARCH,
    val description: String? = null
) {
    /** Helper: calories for a specific gram amount. */
    fun caloriesForGrams(grams: Double): Double =
        (caloriesPer100g ?: 0.0) * (grams / 100.0)

    fun proteinForGrams(grams: Double): Double =
        (proteinPer100g ?: 0.0) * (grams / 100.0)

    fun carbsForGrams(grams: Double): Double =
        (carbsPer100g ?: 0.0) * (grams / 100.0)

    fun fatForGrams(grams: Double): Double =
        (fatPer100g ?: 0.0) * (grams / 100.0)
}