package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The user's weight goal.
 * `direction` is derived from currentWeight vs targetWeight.
 * Never ask the user to select the direction.
 */
data class Goal(
    val id: String,
    val userId: String,
    val currentWeightKgAtGoalSet: Double,
    val targetWeightKg: Double,
    val direction: GoalDirection,
    val effectiveDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)