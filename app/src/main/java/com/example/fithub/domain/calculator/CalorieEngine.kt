package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender
import com.example.fithub.domain.model.GoalDirection

/**
 * Central calorie recommendation engine.
 *
 * Output is a *recommended daily calorie* estimate — the user may accept it
 * or override it in Nutrition Goals. Never silently overwrite a user target.
 *
 * For users below [CalculatorConfig.ADULT_MIN_AGE], only the maintenance
 * estimate is returned (no weight-change adjustment) since adult
 * weight-change formulas are not appropriate for minors.
 */
object CalorieEngine {

    data class Input(
        val age: Int,
        val gender: Gender,
        val heightCm: Double,
        val weightKg: Double,
        val activityLevel: ActivityLevel,
        val goalDirection: GoalDirection
    )

    data class Output(
        val bmrKcal: Double,
        val tdeeKcal: Double,
        val recommendedDailyCalories: Int,
        val goalAdjustmentApplied: Int,
        val adultAdjustmentApplied: Boolean
    )

    fun calculate(input: Input): Output {
        val bmr = calculateBmr(
            age = input.age,
            gender = input.gender,
            heightCm = input.heightCm,
            weightKg = input.weightKg
        )

        val tdee = bmr * input.activityLevel.multiplier

        val isAdult = input.age >= CalculatorConfig.ADULT_MIN_AGE

        val adjustment = if (isAdult) {
            when (input.goalDirection) {
                GoalDirection.LOSE -> CalculatorConfig.GOAL_ADJUST_LOSE
                GoalDirection.MAINTAIN -> CalculatorConfig.GOAL_ADJUST_MAINTAIN
                GoalDirection.GAIN -> CalculatorConfig.GOAL_ADJUST_GAIN
            }
        } else {
            0.0
        }

        val rawRecommended = tdee + adjustment
        val safeRecommended = rawRecommended
            .coerceAtLeast(CalculatorConfig.MIN_RECOMMENDED_CALORIES)

        return Output(
            bmrKcal = bmr,
            tdeeKcal = tdee,
            recommendedDailyCalories = safeRecommended.toInt(),
            goalAdjustmentApplied = adjustment.toInt(),
            adultAdjustmentApplied = isAdult
        )
    }

    /** Mifflin-St Jeor Basal Metabolic Rate. */
    fun calculateBmr(
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double
    ): Double {
        val base = (CalculatorConfig.WEIGHT_FACTOR * weightKg) +
                (CalculatorConfig.HEIGHT_FACTOR * heightCm) -
                (CalculatorConfig.AGE_FACTOR * age)

        return when (gender) {
            Gender.MALE -> base + CalculatorConfig.MALE_OFFSET
            Gender.FEMALE -> base + CalculatorConfig.FEMALE_OFFSET
            Gender.OTHER -> base + CalculatorConfig.OTHER_OFFSET
        }
    }

    /** TDEE without goal adjustment. */
    fun calculateTdee(
        bmr: Double,
        activityLevel: ActivityLevel
    ): Double = bmr * activityLevel.multiplier
}