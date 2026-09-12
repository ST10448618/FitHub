package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A completed (or partial / discarded) execution of a workout plan.
 * Each completion is a separate record — a plan can be used many times.
 */
data class WorkoutSession(
    val id: String,
    val userId: String,
    val workoutPlanId: String,
    val workoutName: String,                // snapshot
    val workoutCategory: WorkoutCategory,   // snapshot for stats
    val exercises: List<CompletedExercise> = emptyList(),
    val durationMinutes: Int = 0,
    val estimatedActivityKcal: Int = 0,
    val status: SessionStatus = SessionStatus.COMPLETED,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime = LocalDateTime.now(),
    val logDate: LocalDate = completedAt.toLocalDate()
)

data class CompletedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val reps: Int = 0,
    val durationSeconds: Int = 0,
    val estimatedActivityKcal: Double = 0.0
)