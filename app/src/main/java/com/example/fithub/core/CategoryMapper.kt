package com.example.fithub.core

/**
 * Maps OpenFoodFacts category tags into FitHub's broad categories.
 * We never expose the raw OFF taxonomy — only these 8 buckets.
 */
object CategoryMapper {

    val FITHUB_CATEGORIES = listOf(
        "Beverages", "Snacks", "Meats", "Fruits",
        "Pasta", "Vegetables", "Dairy", "Grains"
    )

    /** OFF tag mapping — case-insensitive substring match. */
    private val categoryRules: List<Pair<String, List<String>>> = listOf(
        "Beverages" to listOf("beverage", "drink", "water", "juice", "soda", "coffee", "tea"),
        "Snacks" to listOf("snack", "chip", "crisp", "biscuit", "cookie", "candy", "chocolate", "sweet"),
        "Meats" to listOf("meat", "poultry", "chicken", "beef", "pork", "fish", "seafood", "sausage"),
        "Fruits" to listOf("fruit", "apple", "banana", "orange", "berry", "grape"),
        "Pasta" to listOf("pasta", "noodle", "spaghetti", "macaroni", "lasagna"),
        "Vegetables" to listOf("vegetable", "salad", "tomato", "potato", "carrot", "onion"),
        "Dairy" to listOf("dairy", "milk", "cheese", "yogurt", "yoghurt", "butter"),
        "Grains" to listOf("cereal", "grain", "bread", "rice", "wheat", "oat", "flour")
    )

    fun mapOffCategories(tags: List<String>?): String {
        if (tags.isNullOrEmpty()) return "Other"
        val joined = tags.joinToString(" ").lowercase()
        for ((fithub, keywords) in categoryRules) {
            if (keywords.any { joined.contains(it) }) return fithub
        }
        return "Other"
    }

    /**
     * Maps a FitHub category to its OFF category slug for the /category/{slug}.json endpoint.
     */
    /**
     * Maps a FitHub category to the OFF category tag used by the search-filter API.
     * These are the values used in `tag_0=...`.
     */
    fun toOffCategorySlug(fithubCategory: String): String = when (fithubCategory) {
        "Beverages" -> "beverages"
        "Snacks" -> "snacks"
        "Meats" -> "meats"
        "Fruits" -> "fruits"
        "Pasta" -> "pastas"
        "Vegetables" -> "vegetables"
        "Dairy" -> "dairies"
        "Grains" -> "cereals"
        else -> "plant-based-foods"
    }

    /** Emoji + colour fallback while PNGs aren't exported yet. */
    fun emojiFor(category: String): String = when (category) {
        "Beverages" -> "🥤"
        "Snacks" -> "🍟"
        "Meats" -> "🍖"
        "Fruits" -> "🍎"
        "Pasta" -> "🍝"
        "Vegetables" -> "🥗"
        "Dairy" -> "🧀"
        "Grains" -> "🌾"
        else -> "🍽"
    }

    /** Drawable name — returns 0 if not present. Caller falls back to emoji. */
    fun drawableNameFor(category: String): String = when (category) {
        "Beverages" -> "cat_beverages"
        "Snacks" -> "cat_snacks"
        "Meats" -> "cat_meats"
        "Fruits" -> "cat_fruits"
        "Pasta" -> "cat_pasta"
        "Vegetables" -> "cat_vegetables"
        "Dairy" -> "cat_dairy"
        "Grains" -> "cat_grains"
        else -> "food_placeholder"
    }
}