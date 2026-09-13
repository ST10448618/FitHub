package com.example.fithub.ui.screens.goals

import com.example.fithub.domain.model.Goal
import com.example.fithub.domain.model.NutritionGoals
import com.example.fithub.domain.model.WorkoutGoals
import java.time.LocalDate

data class GoalsTargetsUiState(
    val isLoading: Boolean = true,

    val goal: Goal? = null,
    val startingWeightKg: Double? = null,
    val currentWeightKg: Double? = null,

    val nutritionGoals: NutritionGoals? = null,
    val workoutGoals: WorkoutGoals? = null,
    val nextCheckpointDate: LocalDate? = null
)