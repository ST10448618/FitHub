package com.example.fithub.ui.screens.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.domain.catalog.RewardCatalog
import com.example.fithub.ui.theme.*
import com.example.fithub.domain.catalog.RewardOffer

@Composable
fun RewardsScreen(
    onOpenAchievements: () -> Unit,
    viewModel: RewardsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Rewards",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Current Particles:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            "%,d".format(state.particleBalance),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Earn particles banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onOpenAchievements)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Earn Particles",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "while completing achievements by meeting goals!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Open achievements",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Rewards on offer
        Text(
            "Rewards on offer",
            style = MaterialTheme.typography.titleLarge,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.offers.forEach { offer ->
                OfferRow(
                    offer = offer,
                    status = state.statuses[offer.id]
                        ?: RewardCatalog.RewardStatus.INSUFFICIENT
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OfferRow(
    offer: RewardOffer,
    status: RewardCatalog.RewardStatus
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(
        offer.brandDrawableName,
        "drawable",
        context.packageName
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FitHubLightBlue),
                contentAlignment = Alignment.Center
            ) {
                if (resId != 0) {
                    AsyncImage(
                        model = resId,
                        contentDescription = offer.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(
                        offer.title.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (status) {
                        RewardCatalog.RewardStatus.AVAILABLE -> "Available!"
                        RewardCatalog.RewardStatus.CLAIMED -> "Claimed"
                        RewardCatalog.RewardStatus.INSUFFICIENT -> "Insufficient Amount"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (status) {
                        RewardCatalog.RewardStatus.AVAILABLE -> SuccessGreen
                        RewardCatalog.RewardStatus.CLAIMED -> TextSecondary
                        RewardCatalog.RewardStatus.INSUFFICIENT -> ErrorRed
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(4.dp))
                Text(
                    offer.costParticles.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}