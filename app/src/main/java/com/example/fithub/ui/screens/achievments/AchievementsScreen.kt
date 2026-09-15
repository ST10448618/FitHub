package com.example.fithub.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.theme.*

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Achievements", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // -------- Level card --------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Trophy icon — drawable preferred, emoji fallback
                    val trophyRes = context.resources.getIdentifier(
                        "ic_trophy", "drawable", context.packageName
                    )
                    if (trophyRes != 0) {
                        AsyncImage(
                            model = trophyRes,
                            contentDescription = "Trophy",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(72.dp)
                        )
                    } else {
                        Text("🏆", style = MaterialTheme.typography.displayLarge)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Current Level",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Level ${state.currentLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { state.levelProgress },
                            modifier = Modifier.weight(1f).height(8.dp),
                            color = Color(0xFFFFD54F),
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Level ${state.currentLevel + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Receive ${state.particlesToNextLevel} ✨ on next Level Up",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Nutrition Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            AchievementGrid(
                cards = state.nutritionAchievements,
                onClaim = viewModel::claim
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Workout Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            AchievementGrid(
                cards = state.workoutAchievements,
                onClaim = viewModel::claim
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AchievementGrid(
    cards: List<AchievementCard>,
    onClaim: (String) -> Unit
) {
    if (cards.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { card ->
                    AchievementTile(
                        card = card,
                        onClaim = { onClaim(card.definition.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementTile(
    card: AchievementCard,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val claimed = card.userState.isClaimed
    val unlocked = card.userState.isUnlocked
    val context = LocalContext.current

    Card(
        modifier = modifier
            .clickable(enabled = unlocked && !claimed) { onClaim() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (unlocked) FitHubPrimary.copy(alpha = 0.15f)
                        else SurfaceGray.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val medalRes = context.resources.getIdentifier(
                    "ic_medal", "drawable", context.packageName
                )
                when {
                    unlocked && medalRes != 0 -> AsyncImage(
                        model = medalRes,
                        contentDescription = card.definition.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp)
                    )
                    unlocked -> Text(
                        "🎖",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    else -> Text(
                        "🔒",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                card.definition.title,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                card.definition.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                minLines = 2
            )

            Spacer(Modifier.height(8.dp))

            when {
                claimed -> Text(
                    "Claimed ✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
                unlocked -> Text(
                    "Claim ✨ ${card.definition.rewardParticles}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            FitHubLightBlue,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                else -> Text(
                    "${card.userState.progress}/${card.definition.goalValue}  ✨ ${card.definition.rewardParticles}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}