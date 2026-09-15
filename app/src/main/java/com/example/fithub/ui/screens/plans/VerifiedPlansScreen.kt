package com.example.fithub.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun VerifiedPlansScreen(
    onBack: () -> Unit,
    onPlanClick: (String) -> Unit,
    onSeeAllClick: (WorkoutCategory) -> Unit,
    onCreatePlanClick: () -> Unit,
    viewModel: VerifiedPlansViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Workout Plans", onBack = onBack)

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
            // Search + filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search by Name") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { /* Phase 8: filter sheet */ },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Filter")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        "Fitness is a\npersonal journey",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Discover what works for you",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category icons row
            Text(
                "Verified Plans",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            CategoryIconRow(onCategoryClick = { onSeeAllClick(it) })

            Spacer(Modifier.height(24.dp))

            // Per-category sections
            state.plansByCategory
                .toList()
                .sortedBy { (cat, _) -> categoryOrder(cat) }
                .forEach { (category, plans) ->
                    if (plans.isNotEmpty()) {
                        CategorySection(
                            title = categoryLabel(category),
                            plans = plans,
                            onPlanClick = onPlanClick,
                            onSeeAllClick = { onSeeAllClick(category) },
                            context = context
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }

            if (state.plansByCategory.isEmpty()) {
                EmptyState(
                    title = "No plans found",
                    message = "Try a different search term.",
                    emoji = "🔍"
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryIconRow(onCategoryClick: (WorkoutCategory) -> Unit) {
    val categories = listOf(
        WorkoutCategory.STRENGTH to ("💪" to "Strength"),
        WorkoutCategory.CARDIO to ("🏃" to "Cardio"),
        WorkoutCategory.ENDURANCE to ("⏱" to "Endurance"),
        WorkoutCategory.GENERAL_FITNESS to ("🎯" to "General"),
        WorkoutCategory.MOBILITY to ("🧘" to "Mobility")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { (cat, meta) ->
            val (emoji, label) = meta
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardWhite)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    plans: List<com.example.fithub.domain.model.WorkoutPlan>,
    onPlanClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    context: android.content.Context
) {
    Column {
        SectionHeader(
            title = title,
            actionText = "see all",
            onAction = onSeeAllClick
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(plans, key = { it.id }) { plan ->
                WorkoutCardHorizontal(
                    plan = plan,
                    onClick = { onPlanClick(plan.id) },
                    localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                )
            }
        }
    }
}

private fun categoryOrder(cat: WorkoutCategory): Int = when (cat) {
    WorkoutCategory.GENERAL_FITNESS -> 0
    WorkoutCategory.CARDIO -> 1
    WorkoutCategory.STRENGTH -> 2
    WorkoutCategory.ENDURANCE -> 3
    WorkoutCategory.MOBILITY -> 4
    WorkoutCategory.CREATED -> 5
}

private fun categoryLabel(cat: WorkoutCategory): String = when (cat) {
    WorkoutCategory.GENERAL_FITNESS -> "General Fitness"
    WorkoutCategory.CARDIO -> "Cardio"
    WorkoutCategory.STRENGTH -> "Strength"
    WorkoutCategory.ENDURANCE -> "Endurance"
    WorkoutCategory.MOBILITY -> "Mobility"
    WorkoutCategory.CREATED -> "Created"
}