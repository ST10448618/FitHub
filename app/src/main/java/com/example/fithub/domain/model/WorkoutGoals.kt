package com.example.fithub.domain.model

import java.time.LocalDateTime

/**
 * Only two workout goals exist per the master spec.
 */
data class WorkoutGoals(
    val id: String,
    val userId: String,
    val sessionsPerWeek: Int = 4,
    val monthlyActivityGoalKcal: Int = 10000,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)