package com.example.fithub.ui.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.screens.journal.sections.*

@Composable
fun JournalScreen(
    onNavigate: (String) -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        JournalDateHeader(
            selectedDate = state.selectedDate,
            onDateSelected = { viewModel.selectDate(it) }
        )

        Spacer(Modifier.height(16.dp))

        JournalBodyWeightCard(
            currentWeightKg = state.latestWeight?.weightKg,
            targetWeightKg = state.targetWeightKg,
            nextCheckpointDate = state.nextCheckpointDate,
            onViewDetailsClick = { onNavigate(Screen.BODY_WEIGHT) },
            onManageCheckpointsClick = { onNavigate(Screen.CHECKPOINT_MANAGER) }
        )

        Spacer(Modifier.height(16.dp))

        JournalNutritionCard(
            foodLogs = state.foodLogs,
            onViewAllClick = { onNavigate(Screen.listFood("all")) }
        )

        Spacer(Modifier.height(16.dp))

        JournalWorkoutsCard(
            sessions = state.workoutSessions,
            onViewAllClick = { onNavigate(Screen.listWorkouts("all")) }
        )

        Spacer(Modifier.height(24.dp))
    }
}