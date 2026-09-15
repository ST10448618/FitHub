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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.core.WorkoutImageMapper
import com.example.fithub.domain.model.WorkoutSession
import com.example.fithub.ui.components.*
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
        AppHeader(title = "Fitness Hub")

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
                    "Today's Activities",
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
                    StatColumn("🔥", "${state.todayActivityKcal}", "Calories Burnt")
                    StatColumn("⏱", "${state.todayActiveMinutes}m", "Active")
                    StatColumn("⭐", "${state.workoutStreakDays}", "Days")
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Weekly Workout Goal",
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
                        "${state.weeklyGoalCompleted} / ${state.weeklyGoalTarget} Workout Sessions Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(100 - state.weeklyGoalPercent).coerceAtLeast(0)}% left",
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
                    .clickable { onNavigate("create_workout/step1") }
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
                            "Nothing catches your eye? No problem!",
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tailor make your very own workout plan, suited perfectly to your needs!",
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
                    "Workout Plans",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onNavigate("create_workout/step1") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitHubPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Add Plan", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Plans") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Spacer(Modifier.height(20.dp))

            // ---- Your Recent Activities ----
            SectionHeader(
                title = "Your Recent Activities",
                actionText = "more plans",
                onAction = { onNavigate("create_workout/step1") }
            )
            Spacer(Modifier.height(8.dp))
            if (state.recentSessions.isEmpty()) {
                Text(
                    "No recent activity yet.",
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
                                onNavigate("workout_details/${session.workoutPlanId}")
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Created Plans ----
            SectionHeader(
                title = "Created Plans",
                actionText = "view all",
                onAction = { onNavigate("create_workout/step1") }
            )
            Spacer(Modifier.height(8.dp))
            if (state.createdPlans.isEmpty()) {
                EmptyState(
                    title = "No custom workouts yet",
                    message = "Create your own workout to see it here.",
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
                            onClick = { onNavigate("workout_details/${plan.id}") },
                            localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Saved Plans ----
            SectionHeader(
                title = "Saved Plans",
                actionText = "view all",
                onAction = { onNavigate("verified_plans") }
            )
            Spacer(Modifier.height(8.dp))
            if (state.savedPlans.isEmpty()) {
                EmptyState(
                    title = "No saved workouts yet",
                    message = "Browse verified workouts to save one.",
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
                            onClick = { onNavigate("workout_details/${plan.id}") },
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
                "${session.estimatedActivityKcal} kcal burnt",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                "${session.exercises.size} exercises • ${session.durationMinutes} min",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}