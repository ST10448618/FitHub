package com.example.fithub.data.seed

import com.example.fithub.data.local.entity.FoodEntity
import com.example.fithub.domain.model.FoodSource
import java.time.LocalDateTime

/**
 * Pre-seeded common foods so categories work even when OFF is down.
 * Nutrition values are approximate per 100g and are for tracking only.
 */
object FoodSeed {

    fun foods(): List<FoodEntity> {
        val now = LocalDateTime.now()
        fun f(
            id: String, name: String, brand: String?, category: String,
            kcal: Double, p: Double, c: Double, fat: Double,
            servingG: Double? = 100.0, servingLabel: String? = "100g"
        ) = FoodEntity(
            id = id, name = name, brand = brand, imageUrl = null, category = category,
            caloriesPer100g = kcal, proteinPer100g = p, carbsPer100g = c, fatPer100g = fat,
            fiberPer100g = null, sugarPer100g = null, sodiumPer100g = null,
            servingSizeG = servingG, servingLabel = servingLabel,
            source = FoodSource.MANUAL, description = null, cachedAt = now
        )

        return listOf(
            // ---------- Fruits ----------
            f("seed:apple", "Apple", null, "Fruits", 52.0, 0.3, 14.0, 0.2, 182.0, "1 medium"),
            f("seed:banana", "Banana", null, "Fruits", 89.0, 1.1, 23.0, 0.3, 118.0, "1 medium"),
            f("seed:orange", "Orange", null, "Fruits", 47.0, 0.9, 12.0, 0.1, 140.0, "1 medium"),
            f("seed:grapes", "Grapes", null, "Fruits", 69.0, 0.7, 18.0, 0.2, 100.0, "100g"),
            f("seed:pear", "Pear", null, "Fruits", 57.0, 0.4, 15.0, 0.1, 178.0, "1 medium"),
            f("seed:pineapple", "Pineapple", null, "Fruits", 50.0, 0.5, 13.0, 0.1, 100.0, "100g"),
            f("seed:strawberry", "Strawberry", null, "Fruits", 32.0, 0.7, 8.0, 0.3, 100.0, "100g"),
            f("seed:blueberry", "Blueberry", null, "Fruits", 57.0, 0.7, 14.0, 0.3, 100.0, "100g"),

            // ---------- Vegetables ----------
            f("seed:carrot", "Carrot", null, "Vegetables", 41.0, 0.9, 10.0, 0.2, 100.0, "100g"),
            f("seed:broccoli", "Broccoli", null, "Vegetables", 34.0, 2.8, 7.0, 0.4, 100.0, "100g"),
            f("seed:spinach", "Spinach", null, "Vegetables", 23.0, 2.9, 3.6, 0.4, 100.0, "100g"),
            f("seed:tomato", "Tomato", null, "Vegetables", 18.0, 0.9, 3.9, 0.2, 123.0, "1 medium"),
            f("seed:potato", "Potato", null, "Vegetables", 77.0, 2.0, 17.0, 0.1, 100.0, "100g"),
            f("seed:onion", "Onion", null, "Vegetables", 40.0, 1.1, 9.0, 0.1, 100.0, "100g"),

            // ---------- Meats ----------
            f("seed:chicken_breast", "Chicken Breast", null, "Meats", 165.0, 31.0, 0.0, 3.6, 100.0, "100g"),
            f("seed:beef_steak", "Beef Steak", null, "Meats", 271.0, 25.0, 0.0, 19.0, 100.0, "100g"),
            f("seed:pork_chop", "Pork Chop", null, "Meats", 242.0, 27.0, 0.0, 14.0, 100.0, "100g"),
            f("seed:salmon", "Salmon", null, "Meats", 208.0, 20.0, 0.0, 13.0, 100.0, "100g"),
            f("seed:tuna", "Tuna", null, "Meats", 132.0, 28.0, 0.0, 1.3, 100.0, "100g"),

            // ---------- Dairy ----------
            f("seed:milk", "Milk (whole)", null, "Dairy", 61.0, 3.2, 4.8, 3.3, 244.0, "1 cup"),
            f("seed:cheese_cheddar", "Cheddar Cheese", null, "Dairy", 402.0, 25.0, 1.3, 33.0, 30.0, "1 slice"),
            f("seed:yogurt", "Yogurt (plain)", null, "Dairy", 61.0, 3.5, 4.7, 3.3, 170.0, "1 cup"),
            f("seed:butter", "Butter", null, "Dairy", 717.0, 0.9, 0.1, 81.0, 14.0, "1 tbsp"),

            // ---------- Grains ----------
            f("seed:white_rice", "White Rice (cooked)", null, "Grains", 130.0, 2.7, 28.0, 0.3, 158.0, "1 cup"),
            f("seed:brown_rice", "Brown Rice (cooked)", null, "Grains", 123.0, 2.7, 26.0, 1.0, 195.0, "1 cup"),
            f("seed:bread_white", "White Bread", null, "Grains", 265.0, 9.0, 49.0, 3.2, 25.0, "1 slice"),
            f("seed:oats", "Oats (dry)", null, "Grains", 389.0, 17.0, 66.0, 6.9, 40.0, "1/2 cup"),
            f("seed:quinoa", "Quinoa (cooked)", null, "Grains", 120.0, 4.4, 21.0, 1.9, 185.0, "1 cup"),

            // ---------- Pasta ----------
            f("seed:spaghetti", "Spaghetti (cooked)", null, "Pasta", 158.0, 5.8, 31.0, 0.9, 140.0, "1 cup"),
            f("seed:penne", "Penne (cooked)", null, "Pasta", 158.0, 5.8, 31.0, 0.9, 140.0, "1 cup"),
            f("seed:lasagna", "Lasagna", null, "Pasta", 135.0, 7.0, 15.0, 5.0, 200.0, "1 piece"),

            // ---------- Snacks ----------
            f("seed:potato_chips", "Potato Chips", null, "Snacks", 536.0, 7.0, 53.0, 34.0, 28.0, "1 handful"),
            f("seed:chocolate_bar", "Chocolate Bar", null, "Snacks", 546.0, 7.6, 61.0, 31.0, 45.0, "1 bar"),
            f("seed:cookie", "Cookie", null, "Snacks", 502.0, 6.0, 66.0, 24.0, 30.0, "1 cookie"),
            f("seed:crackers", "Crackers", null, "Snacks", 421.0, 9.0, 74.0, 10.0, 30.0, "5 crackers"),

            // ---------- Beverages ----------
            f("seed:water", "Water", null, "Beverages", 0.0, 0.0, 0.0, 0.0, 250.0, "1 cup"),
            f("seed:coffee_black", "Black Coffee", null, "Beverages", 2.0, 0.3, 0.0, 0.0, 240.0, "1 cup"),
            f("seed:orange_juice", "Orange Juice", null, "Beverages", 45.0, 0.7, 10.4, 0.2, 248.0, "1 cup"),
            f("seed:cola", "Cola", null, "Beverages", 42.0, 0.0, 10.6, 0.0, 355.0, "1 can"),
            f("seed:tea", "Tea (unsweetened)", null, "Beverages", 1.0, 0.0, 0.2, 0.0, 240.0, "1 cup")
        )
    }
}