package com.example.fithub.ui.screens.plans.create

import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutDifficulty
import com.example.fithub.domain.model.WorkoutExercise

/**
 * In-memory draft for the 4-step Create Workout flow.
 * Persists across the 4 screens; cleared on successful creation or cancel.
 *
 * If you later want to move this to a graph-scoped ViewModel, this object
 * is the single place to change.
 */
object CreateWorkoutSession {

    data class Draft(
        val name: String = "",
        val description: String = "",
        val difficulty: WorkoutDifficulty = WorkoutDifficulty.BEGINNER,
        val equipment: String = "None",
        val imageUrl: String? = null,
        val category: WorkoutCategory = WorkoutCategory.CREATED,
        val exercises: List<WorkoutExercise> = emptyList()
    )

    private var _draft: Draft = Draft()
    val draft: Draft get() = _draft

    fun update(block: (Draft) -> Draft) {
        _draft = block(_draft)
    }

    fun reset() {
        _draft = Draft()
    }

    fun isStep1Valid(): Boolean = _draft.name.isNotBlank()

    fun isStep2Valid(): Boolean = _draft.exercises.isNotEmpty()

    /** Renumber exercises so orderIndex is 0n-1. */
    fun normaliseOrder() {
        _draft = _draft.copy(
            exercises = _draft.exercises.mapIndexed { idx, ex ->
                ex.copy(orderIndex = idx)
            }
        )
    }
}