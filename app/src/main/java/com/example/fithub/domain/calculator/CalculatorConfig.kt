package com.example.fithub.domain.calculator

/**
 * Prototype configuration constants.
 * These are NOT medical prescriptions — they are documented prototype values.
 * Change them in one place; every calculator reads from here.
 */
object CalculatorConfig {

    // ---- Mifflin-St Jeor constants ----
    const val WEIGHT_FACTOR = 10.0
    const val HEIGHT_FACTOR = 6.25
    const val AGE_FACTOR = 5.0
    const val MALE_OFFSET = 5.0
    const val FEMALE_OFFSET = -161.0
    const val OTHER_OFFSET = -78.0   // midpoint of male/female

    // ---- Goal adjustments (kcal/day) ----
    const val GOAL_ADJUST_LOSE = -250.0
    const val GOAL_ADJUST_MAINTAIN = 0.0
    const val GOAL_ADJUST_GAIN = 250.0

    // ---- Goal direction tolerance (kg) ----
    // Within this range, we consider the goal "maintain".
    const val MAINTAIN_TOLERANCE_KG = 0.5

    // ---- Adult minimum age ----
    // Weight-change adjustments are only applied at/above this age.
    // Below it, we return the maintenance-calorie estimate only.
    const val ADULT_MIN_AGE = 18

    // ---- Macro starter distribution (fractions of daily calories) ----
    const val MACRO_PROTEIN_FRACTION = 0.20
    const val MACRO_CARB_FRACTION = 0.50
    const val MACRO_FAT_FRACTION = 0.30

    // ---- Calorie constants ----
    const val KCAL_PER_G_PROTEIN = 4.0
    const val KCAL_PER_G_CARB = 4.0
    const val KCAL_PER_G_FAT = 9.0

    // ---- Default meal split (fractions of daily calories) ----
    const val MEAL_BREAKFAST_FRACTION = 0.24
    const val MEAL_LUNCH_FRACTION = 0.26
    const val MEAL_DINNER_FRACTION = 0.30
    const val MEAL_SNACK_FRACTION = 0.20

    // ---- Safety minimum ----
    // Never recommend fewer than this many kcal/day in the prototype.
    const val MIN_RECOMMENDED_CALORIES = 1200.0
}