package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.MacroTargets
import com.example.fithub.domain.model.MealTargets
import kotlin.math.roundToInt

/**
 * Produces *starter* macro and meal targets from a daily calorie target.
 * These are user-editable tracking goals — not medical prescriptions.
 */
object MacroCalculator {

    /** Starter macro distribution (20% protein / 50% carbs / 30% fat). */
    fun starterMacros(dailyCalories: Int): MacroTargets {
        val proteinKcal = dailyCalories * CalculatorConfig.MACRO_PROTEIN_FRACTION
        val carbKcal = dailyCalories * CalculatorConfig.MACRO_CARB_FRACTION
        val fatKcal = dailyCalories * CalculatorConfig.MACRO_FAT_FRACTION

        return MacroTargets(
            proteinG = (proteinKcal / CalculatorConfig.KCAL_PER_G_PROTEIN).roundToInt(),
            carbsG = (carbKcal / CalculatorConfig.KCAL_PER_G_CARB).roundToInt(),
            fatG = (fatKcal / CalculatorConfig.KCAL_PER_G_FAT).roundToInt()
        )
    }

    /**
     * Default meal split (24% breakfast, 26% lunch, 30% dinner, 20% snack).
     * Total always equals the input daily calories.
     */
    fun starterMealTargets(dailyCalories: Int): MealTargets {
        val breakfast = (dailyCalories * CalculatorConfig.MEAL_BREAKFAST_FRACTION).roundToInt()
        val lunch = (dailyCalories * CalculatorConfig.MEAL_LUNCH_FRACTION).roundToInt()
        val dinner = (dailyCalories * CalculatorConfig.MEAL_DINNER_FRACTION).roundToInt()
        val snack = dailyCalories - breakfast - lunch - dinner   // absorb rounding error

        return MealTargets(
            breakfastKcal = breakfast,
            lunchKcal = lunch,
            dinnerKcal = dinner,
            snackKcal = snack
        )
    }

    /** True when the meal allocation sums exactly to the daily target. */
    fun isMealAllocationValid(dailyCalories: Int, meals: MealTargets): Boolean =
        meals.total == dailyCalories
}