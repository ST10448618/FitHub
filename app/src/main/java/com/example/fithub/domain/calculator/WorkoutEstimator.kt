package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.Exercise
import com.example.fithub.domain.model.WorkoutExercise
import kotlin.math.ceil

/**
 * Estimated activity and duration for exercises and full workouts.
 * All values are prototype estimates, not measured values.
 */
object WorkoutEstimator {

    /**
     * Estimated kcal for a single exercise slot.
     * Time-based exercises use targetDurationSeconds; rep-based use targetReps.
     */
    fun exerciseActivity(
        exercise: Exercise,
        targetReps: Int,
        targetDurationSeconds: Int
    ): Double {
        return if (exercise.isTimeBased) {
            exercise.estimatedActivityPerSecond * targetDurationSeconds
        } else {
            exercise.estimatedActivityPerRep * targetReps
        }
    }

    /**
     * Estimated seconds for one exercise slot.
     * Time-based: exactly the target duration.
     * Rep-based: secondsPerRep × reps.
     */
    fun exerciseDurationSeconds(
        exercise: Exercise,
        targetReps: Int,
        targetDurationSeconds: Int
    ): Int {
        return if (exercise.isTimeBased) {
            targetDurationSeconds
        } else {
            exercise.estimatedSecondsPerRep * targetReps
        }
    }

    /** Sum of exercise activity. */
    fun totalActivity(exercises: List<WorkoutExercise>): Int =
        exercises.sumOf { it.estimatedActivityKcal }.toInt()

    /**
     * Total duration in minutes, rounded up.
     * Adds a small fixed transition buffer per exercise to feel realistic.
     */
    fun totalDurationMinutes(
        exercises: List<WorkoutExercise>,
        exerciseCatalog: Map<String, Exercise>
    ): Int {
        if (exercises.isEmpty()) return 0
        val seconds = exercises.sumOf { slot ->
            val catalog = exerciseCatalog[slot.exerciseId]
            val exerciseSeconds = if (catalog != null) {
                exerciseDurationSeconds(
                    exercise = catalog,
                    targetReps = slot.targetReps,
                    targetDurationSeconds = slot.targetDurationSeconds
                )
            } else {
                // Fallback for unknown exercises: assume 3s per rep
                slot.targetReps * 3
            }
            exerciseSeconds
        }
        // Add 20 seconds transition per exercise
        val transitions = exercises.size * 20
        return ceil((seconds + transitions) / 60.0).toInt()
    }

    /** Recompute WorkoutExercise activity values using the current exercise dataset. */
    fun recalculate(
        exercises: List<WorkoutExercise>,
        exerciseCatalog: Map<String, Exercise>
    ): List<WorkoutExercise> = exercises.map { slot ->
        val catalog = exerciseCatalog[slot.exerciseId]
        val activity = if (catalog != null) {
            exerciseActivity(catalog, slot.targetReps, slot.targetDurationSeconds)
        } else {
            slot.estimatedActivityKcal
        }
        slot.copy(estimatedActivityKcal = activity)
    }
}