package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fithub.R
import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.model.MealType
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

@Composable
fun JournalNutritionCard(
    foodLogs: List<FoodLog>,
    onViewAllClick: () -> Unit,
    onLogClick: (FoodLog) -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.journal_nutrition_title),
            actionText = stringResource(R.string.common_view_all),
            onAction = onViewAllClick
        )

        if (foodLogs.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.journal_nutrition_empty_title),
                message = stringResource(R.string.journal_nutrition_empty_body),
                emoji = "🍽"
            )
        } else {
            val visible = foodLogs.take(4)
            visible.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { log ->
                        FoodLogTile(
                            log = log,
                            onClick = { onLogClick(log) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (foodLogs.size > 4) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(
                        R.string.journal_nutrition_more_format,
                        foodLogs.size - 4
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FoodLogTile(
    log: FoodLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealColor = when (log.mealType) {
        MealType.BREAKFAST -> MealBreakfast
        MealType.LUNCH -> MealLunch
        MealType.DINNER -> MealDinner
        MealType.SNACK -> MealSnack
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
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
                    Text("🍽", style = MaterialTheme.typography.displayLarge)
                }
            }

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(mealColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.mealType.localized(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.food_log_row_calories_format, log.calories.toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}