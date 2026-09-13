package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.GoalDirection

/**
 * Percentage progress from starting weight toward target weight.
 * Correct for both gain and loss. Never divides by zero.
 */
object WeightProgressCalculator {

    data class Result(
        val rawPercent: Double,      // unbounded (can be <0 or >100)
        val clampedPercent: Int,     // 0..100 for display
        val remainingKg: Double,     // kg still to go
        val deltaFromStartKg: Double // how far we've moved from start
    )

    fun calculate(
        startingWeightKg: Double,
        currentWeightKg: Double,
        targetWeightKg: Double,
        direction: GoalDirection
    ): Result {
        val deltaFromStart = currentWeightKg - startingWeightKg

        val totalDistance = when (direction) {
            GoalDirection.GAIN -> targetWeightKg - startingWeightKg
            GoalDirection.LOSE -> startingWeightKg - targetWeightKg
            GoalDirection.MAINTAIN -> 0.0
        }

        val travelled = when (direction) {
            GoalDirection.GAIN -> currentWeightKg - startingWeightKg
            GoalDirection.LOSE -> startingWeightKg - currentWeightKg
            GoalDirection.MAINTAIN -> 0.0
        }

        val rawPercent = if (totalDistance <= 0.0) {
            // Maintain: if current is close to target, count as 100%.
            if (direction == GoalDirection.MAINTAIN &&
                kotlin.math.abs(targetWeightKg - currentWeightKg) <=
                CalculatorConfig.MAINTAIN_TOLERANCE_KG) 100.0 else 0.0
        } else {
            (travelled / totalDistance) * 100.0
        }

        val remainingKg = when (direction) {
            GoalDirection.GAIN -> (targetWeightKg - currentWeightKg).coerceAtLeast(0.0)
            GoalDirection.LOSE -> (currentWeightKg - targetWeightKg).coerceAtLeast(0.0)
            GoalDirection.MAINTAIN -> 0.0
        }

        return Result(
            rawPercent = rawPercent,
            clampedPercent = rawPercent.toInt().coerceIn(0, 100),
            remainingKg = remainingKg,
            deltaFromStartKg = deltaFromStart
        )
    }
}