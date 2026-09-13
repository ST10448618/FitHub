package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.GoalDirection
import kotlin.math.abs

/**
 * Derives the goal direction from current vs target weight.
 * The user never selects this manually.
 */
object GoalDirectionCalculator {

    fun from(
        currentWeightKg: Double,
        targetWeightKg: Double
    ): GoalDirection {
        val delta = targetWeightKg - currentWeightKg
        return when {
            abs(delta) <= CalculatorConfig.MAINTAIN_TOLERANCE_KG ->
                GoalDirection.MAINTAIN
            delta < 0.0 -> GoalDirection.LOSE
            else -> GoalDirection.GAIN
        }
    }

    fun label(direction: GoalDirection): String = when (direction) {
        GoalDirection.LOSE -> "Lose Weight"
        GoalDirection.MAINTAIN -> "Maintain Weight"
        GoalDirection.GAIN -> "Gain Weight"
    }
}