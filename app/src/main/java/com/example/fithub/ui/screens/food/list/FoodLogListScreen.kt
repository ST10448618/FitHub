package com.example.fithub.ui.screens.food.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.model.MealType
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.theme.*

@Composable
fun FoodLogListScreen(
    onBack: () -> Unit,
    onLogClick: (FoodLog) -> Unit,
    viewModel: FoodLogListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(
            title = "Nutrition · ${state.dateLabel}",
            onBack = onBack
        )

        when {
            state.isLoading -> LoadingState()
            state.logs.isEmpty() -> EmptyState(
                title = "No meals logged",
                message = "Nothing recorded for this day.",
                emoji = "🍽"
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.logs, key = { it.id }) { log ->
                    FoodLogRow(log = log, onClick = { onLogClick(log) })
                }
            }
        }
    }
}

@Composable
private fun FoodLogRow(log: FoodLog, onClick: () -> Unit) {
    val mealColor = when (log.mealType) {
        MealType.BREAKFAST -> MealBreakfast
        MealType.LUNCH -> MealLunch
        MealType.DINNER -> MealDinner
        MealType.SNACK -> MealSnack
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (log.imageUrl != null) {
                AsyncImage(
                    model = log.imageUrl,
                    contentDescription = log.foodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(FitHubMidBlue, FitHubPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(mealColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = log.mealType.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                text = "${log.calories.toInt()} cal",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}