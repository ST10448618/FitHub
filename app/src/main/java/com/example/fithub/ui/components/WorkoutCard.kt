package com.example.fithub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fithub.domain.model.WorkoutPlan
import com.example.fithub.ui.theme.*

@Composable
fun WorkoutCardHorizontal(
    plan: WorkoutPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    localImageRes: Int = 0
) {
    val imageModel: Any? = when {
        plan.imageUrl != null -> plan.imageUrl
        localImageRes != 0 -> localImageRes
        else -> null
    }

    Box(
        modifier = modifier
            .width(210.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = plan.name,
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
                Text(
                    plan.name.take(1).uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Gradient scrim for legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                plan.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${plan.exercises.size} exercises • ${plan.estimatedDurationMinutes} min",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "${plan.estimatedActivityKcal} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        if (plan.isVerified) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FitHubPrimary.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkoutCardRow(
    plan: WorkoutPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    localImageRes: Int = 0
) {
    val imageModel: Any? = when {
        plan.imageUrl != null -> plan.imageUrl
        localImageRes != 0 -> localImageRes
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FitHubLightBlue),
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = plan.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    plan.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                plan.name,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${plan.estimatedDurationMinutes} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            "${plan.estimatedActivityKcal} kcal",
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}