package com.example.fithub.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.screens.dashboard.sections.*
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage ?: "", color = TextSecondary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        DashboardHero(
            profile = state.profile,
            onProfileClick = onProfileClick,
            onHeroClick = { onNavigate(Screen.PROGRESS_OVERVIEW) }
        )

        Spacer(Modifier.height(16.dp))

        TodaysOverviewSection(
            snapshot = state.nutritionSnapshot,
            onAddMealClick = { onNavigate(Screen.ADD_MEAL) },
            onAddWorkoutClick = { onNavigate(Screen.PLANS) }
        )

        Spacer(Modifier.height(20.dp))

        RecommendedActivitiesSection(
            plans = state.recommendedPlans,
            onPlanClick = { plan -> onNavigate(Screen.workoutDetails(plan.id)) },
            onViewPlansClick = { onNavigate(Screen.PLANS) }
        )

        Spacer(Modifier.height(20.dp))

        CalorieIntakeChartSection(
            weeklyCalories = state.weeklyCalories,
            dailyTarget = state.dailyCalorieTarget,
            onViewMoreClick = { onNavigate(Screen.NUTRITION_OVERVIEW) }
        )

        Spacer(Modifier.height(20.dp))

        TodaysMealsSection(
            snapshot = state.nutritionSnapshot,
            onViewMoreClick = { onNavigate(Screen.NUTRITION_OVERVIEW) }
        )

        Spacer(Modifier.height(20.dp))

        WeeklyWorkoutGoalSection(
            weeklyCompleted = state.weeklyWorkoutCompleted,
            weeklyTarget = state.weeklyWorkoutTarget,
            weeklyProgress = state.weeklyWorkoutProgressPercent,
            onViewMoreClick = { onNavigate(Screen.WORKOUTS_OVERVIEW) }
        )

        Spacer(Modifier.height(24.dp))
    }
}