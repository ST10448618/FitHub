package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.core.OnboardingSession
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.ui.theme.CardWhite
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import com.example.fithub.util.localized

@Composable
fun OnboardingStep7Lifestyle(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var selected by remember { mutableStateOf(OnboardingSession.activityLevel) }

    OnboardingScaffold(
        stepNumber = 7,
        title = stringResource(R.string.onboarding_step7_title),
        subtitle = stringResource(R.string.onboarding_step7_subtitle),
        onCancel = onCancel,
        onNext = {
            OnboardingSession.activityLevel = selected
            onNext()
        },
        nextEnabled = selected != null,
        nextButtonText = stringResource(R.string.onboarding_step7_next)
    ) {
        val options = listOf(
            ActivityLevel.SEDENTARY to R.string.onboarding_step7_desc_sedentary,
            ActivityLevel.LIGHTLY_ACTIVE to R.string.onboarding_step7_desc_lightly,
            ActivityLevel.MODERATELY_ACTIVE to R.string.onboarding_step7_desc_moderately,
            ActivityLevel.VERY_ACTIVE to R.string.onboarding_step7_desc_very,
            ActivityLevel.EXTREMELY_ACTIVE to R.string.onboarding_step7_desc_extremely
        )

        options.forEach { (level, descriptionRes) ->
            LifestyleRow(
                title = level.localized(),
                description = stringResource(descriptionRes),
                selected = selected == level,
                onClick = { selected = level }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LifestyleRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) FitHubPrimary else Color.LightGray,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FitHubPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📊", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}