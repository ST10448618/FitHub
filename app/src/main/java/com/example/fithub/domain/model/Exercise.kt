package com.example.fithub.domain.model

/**
 * An entry in the seeded exercise dataset.
 * `estimatedActivityPerRep` is a prototype estimate (kcal), not a medical value.
 * For time-based exercises (e.g. Plank), use `estimatedActivityPerSecond` instead.
 */
data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val description: String = "",
    val difficulty: WorkoutDifficulty = WorkoutDifficulty.BEGINNER,
    val equipment: String = "None",
    val isTimeBased: Boolean = false,

    // Prototype calorie estimates
    val estimatedActivityPerRep: Double = 0.5,      // kcal per rep
    val estimatedActivityPerSecond: Double = 0.1,   // kcal per second (if time-based)
    val estimatedSecondsPerRep: Int = 3,            // how long a rep takes

    val imageUrl: String? = null
)