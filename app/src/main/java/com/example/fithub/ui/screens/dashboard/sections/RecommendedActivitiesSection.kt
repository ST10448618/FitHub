package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.core.WorkoutImageMapper
import com.example.fithub.domain.model.WorkoutPlan
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.components.WorkoutCardHorizontal
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun RecommendedActivitiesSection(
    plans: List<WorkoutPlan>,
    onPlanClick: (WorkoutPlan) -> Unit,
    onViewPlansClick: () -> Unit
) {
    val context = LocalContext.current

    Column {
        SectionHeader(
            title = stringResource(R.string.dashboard_recommended_title),
            actionText = stringResource(R.string.dashboard_recommended_action),
            onAction = onViewPlansClick
        )
        if (plans.isEmpty()) {
            Text(
                stringResource(R.string.dashboard_recommended_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(plans, key = { it.id }) { plan ->
                    WorkoutCardHorizontal(
                        plan = plan,
                        onClick = { onPlanClick(plan) },
                        localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                    )
                }
            }
        }
    }
}