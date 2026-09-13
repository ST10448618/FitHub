package com.example.fithub.ui.screens.goals

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
import com.example.fithub.ui.screens.goals.sections.*

@Composable
fun GoalsTargetsScreen(
    onNavigate: (String) -> Unit,
    viewModel: GoalsTargetsViewModel = viewModel()
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
        GoalJourneyCard(
            startingWeightKg = state.startingWeightKg,
            currentWeightKg = state.currentWeightKg,
            goal = state.goal,
            onManageClick = { onNavigate(Screen.WEIGHT_GOAL) }
        )

        Spacer(Modifier.height(16.dp))

        NutritionGoalsCard(
            goals = state.nutritionGoals,
            onManageClick = { onNavigate(Screen.NUTRITION_GOALS) }
        )

        Spacer(Modifier.height(16.dp))

        WorkoutGoalsCard(
            goals = state.workoutGoals,
            onManageClick = { onNavigate(Screen.WORKOUT_GOALS) }
        )

        Spacer(Modifier.height(16.dp))

        CheckpointSummaryCard(
            nextCheckpointDate = state.nextCheckpointDate,
            onManageClick = { onNavigate(Screen.CHECKPOINT_MANAGER) }
        )

        Spacer(Modifier.height(24.dp))
    }
}