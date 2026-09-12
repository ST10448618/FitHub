package com.example.fithub.domain.model

import java.time.LocalDateTime

/**
 * A reusable workout session.
 * NOT a multi-week programme.
 * NOT a set-based system — reps and duration only.
 */
data class WorkoutPlan(
    val id: String,
    val userId: String? = null,             // null = verified library plan
    val name: String,
    val description: String = "",
    val category: WorkoutCategory = WorkoutCategory.GENERAL_FITNESS,
    val difficulty: WorkoutDifficulty = WorkoutDifficulty.BEGINNER,
    val equipment: String = "None",
    val imageUrl: String? = null,

    val exercises: List<WorkoutExercise> = emptyList(),

    val estimatedActivityKcal: Int = 0,     // computed from exercises
    val estimatedDurationMinutes: Int = 0,  // computed from exercises

    val isVerified: Boolean = false,        // true = shipped with the app
    val isSaved: Boolean = false,           // true = user saved it from library
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * An exercise slot inside a workout plan.
 * `targetReps` OR `targetDurationSeconds` is used depending on the exercise type.
 */
data class WorkoutExercise(
    val exerciseId: String,
    val exerciseName: String,               // snapshot for resilience
    val category: ExerciseCategory,
    val orderIndex: Int,
    val targetReps: Int = 10,
    val targetDurationSeconds: Int = 0,
    val estimatedActivityKcal: Double = 0.0 // computed from the exercise dataset
)