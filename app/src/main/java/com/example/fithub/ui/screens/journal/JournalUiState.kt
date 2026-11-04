package com.example.fithub.ui.screens.journal

import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.domain.model.WorkoutSession
import java.time.LocalDate

data class JournalUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val latestWeight: WeightEntry? = null,
    val targetWeightKg: Double? = null,
    val nextCheckpointDate: LocalDate? = null,
    val foodLogs: List<FoodLog> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList()
)