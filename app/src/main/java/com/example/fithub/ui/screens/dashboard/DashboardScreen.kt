package com.example.fithub.ui.screens.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.notifications.NotificationPermissionHelper
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.screens.dashboard.sections.*
import com.example.fithub.ui.theme.TextSecondary
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        scope.launch { ServiceLocator.preferencesManager.setNotificationRationaleShown(true) }
        if (granted) {
            scope.launch {
                ServiceLocator.preferencesManager.setPushNotifications(true)
                FirebaseMessaging.getInstance().subscribeToTopic("fithub_all")
            }
        }
    }

    LaunchedEffect(Unit) {
        val shown = ServiceLocator.preferencesManager.notificationRationaleShown.first()
        if (!shown && !NotificationPermissionHelper.isGranted(context)) {
            showRationale = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                scope.launch { ServiceLocator.preferencesManager.setNotificationRationaleShown(true) }
            },
            title = { Text(stringResource(R.string.notif_rationale_title)) },
            text = { Text(stringResource(R.string.notif_rationale_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scope.launch {
                            ServiceLocator.preferencesManager.setNotificationRationaleShown(true)
                            ServiceLocator.preferencesManager.setPushNotifications(true)
                            FirebaseMessaging.getInstance().subscribeToTopic("fithub_all")
                        }
                    }
                }) { Text(stringResource(R.string.notif_rationale_accept)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    scope.launch { ServiceLocator.preferencesManager.setNotificationRationaleShown(true) }
                }) { Text(stringResource(R.string.notif_rationale_decline)) }
            }
        )
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

        // Phase 6 (UDF 5): streaks + weekly challenge
        StreakCard(
            currentStreak = state.currentStreak,
            longestStreak = state.longestStreak,
            todayDone = state.todayDone,
            atRisk = state.streakAtRisk
        )

        Spacer(Modifier.height(12.dp))

        WeeklyChallengeCard(
            completed = state.challengeCompleted,
            target = state.challengeTarget
        )

        Spacer(Modifier.height(20.dp))

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