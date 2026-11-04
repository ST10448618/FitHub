package com.example.fithub.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.ui.theme.CardWhite
import com.example.fithub.ui.theme.FitHubLightBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

/**
 * Main bottom-navigation tabs.
 *
 * Labels are stored as `@param:StringRes` (explicit `@param:` target avoids a
 * future Kotlin annotation-target default change — see KT-73255):
 * enum constants cannot call `stringResource(...)`, so the composable resolves
 * the resource at render time and re-translates on configuration change.
 *
 * `Icons.AutoMirrored.Filled.MenuBook` is the non-deprecated replacement for
 * `Icons.Filled.MenuBook` (RTL-aware) — recommended by the Material Icons
 * migration guide.
 *
 * Reference: https://youtrack.jetbrains.com/issue/KT-73255
 *            https://developer.android.com/jetpack/compose/designsystems/material3
 * (accessed February 2026)
 */
enum class BottomNavItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HOME("dashboard", R.string.nav_home, Icons.Filled.Home),
    JOURNAL("journal", R.string.nav_journal, Icons.AutoMirrored.Filled.MenuBook),
    PLANS("plans", R.string.nav_plans, Icons.Filled.FitnessCenter),
    GOALS("goals", R.string.nav_goals, Icons.Filled.TrackChanges),
    REWARDS("RewardsScreen", R.string.nav_rewards, Icons.Filled.EmojiEvents)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            val tint = if (selected) FitHubPrimary else TextSecondary
            val label = stringResource(item.labelRes)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(item) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (selected) FitHubLightBlue else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}