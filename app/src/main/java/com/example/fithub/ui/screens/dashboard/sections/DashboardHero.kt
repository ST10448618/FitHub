package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.domain.model.UserProfile
import com.example.fithub.ui.theme.FitHubLightBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun DashboardHero(
    profile: UserProfile?,
    onProfileClick: () -> Unit,
    onHeroClick: () -> Unit
) {
    val fallbackName = stringResource(R.string.dashboard_user_fallback)
    val displayName = buildString {
        append(profile?.firstName?.ifBlank { fallbackName } ?: fallbackName)
        val surname = profile?.surname.orEmpty()
        if (surname.isNotBlank()) {
            append(' ')
            append(surname)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHeroClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FitHubLightBlue)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.dashboard_profile_desc),
                tint = FitHubPrimary
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.dashboard_greeting_morning),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}