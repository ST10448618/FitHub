package com.example.fithub.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.WorkoutImageMapper
import com.example.fithub.domain.model.WorkoutSession
import com.example.fithub.ui.components.*
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.theme.*

@Composable
fun PlansScreen(
    onNavigate: (String) -> Unit,
    viewModel: PlansViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.plans_title))

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ---- Today's Activities ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.plans_today_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    java.time.LocalDate.now().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn("🔥", state.todayActivityKcal.toString(),
                        stringResource(R.string.plans_stat_calories))
                    StatColumn("⏱", "${state.todayActiveMinutes}m",
                        stringResource(R.string.plans_stat_active))
                    StatColumn("⭐", state.workoutStreakDays.toString(),
                        stringResource(R.string.plans_stat_days))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.plans_weekly_goal_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                RoundedProgressBar(
                    progress = state.weeklyGoalPercent / 100f,
                    height = 8.dp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(
                            R.string.plans_weekly_sessions_format,
                            state.weeklyGoalCompleted,
                            state.weeklyGoalTarget
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(
                            R.string.plans_weekly_left_format,
                            (100 - state.weeklyGoalPercent).coerceAtLeast(0)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Nothing catches your eye banner ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardWhite)
                    .clickable { onNavigate(Screen.CREATE_WORKOUT_1) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FitHubLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = FitHubPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.plans_custom_banner_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.plans_custom_banner_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = FitHubPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Workout Plans header ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.plans_workout_plans_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onNavigate(Screen.CREATE_WORKOUT_1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitHubPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        stringResource(R.string.plans_add_plan),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.plans_search_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Spacer(Modifier.height(20.dp))

            // ---- Your Recent Activities ----
            SectionHeader(
                title = stringResource(R.string.plans_recent_title),
                actionText = stringResource(R.string.plans_recent_action),
                onAction = { onNavigate(Screen.CREATE_WORKOUT_1) }
            )
            Spacer(Modifier.height(8.dp))
            if (state.recentSessions.isEmpty()) {
                Text(
                    stringResource(R.string.plans_recent_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.recentSessions, key = { it.id }) { session ->
                        RecentSessionCard(
                            session = session,
                            onClick = {
                                onNavigate(Screen.workoutDetails(session.workoutPlanId))
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Created Plans ----
            SectionHeader(
                title = stringResource(R.string.plans_created_title),
                actionText = stringResource(R.string.common_view_all),
                onAction = { onNavigate(Screen.CREATE_WORKOUT_1) }
            )
            Spacer(Modifier.height(8.dp))
            if (state.createdPlans.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.plans_created_empty_title),
                    message = stringResource(R.string.plans_created_empty_body),
                    emoji = "🛠"
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.createdPlans, key = { it.id }) { plan ->
                        WorkoutCardHorizontal(
                            plan = plan,
                            onClick = { onNavigate(Screen.workoutDetails(plan.id)) },
                            localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Saved Plans ----
            SectionHeader(
                title = stringResource(R.string.plans_saved_title),
                actionText = stringResource(R.string.common_view_all),
                onAction = { onNavigate(Screen.VERIFIED_PLANS) }
            )
            Spacer(Modifier.height(8.dp))
            if (state.savedPlans.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.plans_saved_empty_title),
                    message = stringResource(R.string.plans_saved_empty_body),
                    emoji = "⭐"
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.savedPlans, key = { it.id }) { plan ->
                        WorkoutCardHorizontal(
                            plan = plan,
                            onClick = { onNavigate(Screen.workoutDetails(plan.id)) },
                            localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatColumn(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun RecentSessionCard(session: WorkoutSession, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(230.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(listOf(FitHubMidBlue, FitHubPrimary))
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                session.workoutName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.plans_kcal_burnt_format, session.estimatedActivityKcal),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                stringResource(
                    R.string.plans_exercises_minutes_format,
                    session.exercises.size,
                    session.durationMinutes
                ),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}