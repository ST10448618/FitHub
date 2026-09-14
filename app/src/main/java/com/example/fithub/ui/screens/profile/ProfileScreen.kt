package com.example.fithub.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Profile", onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(FitHubMidBlue, FitHubPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Details card
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Your Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            FitHubPrimary,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                DetailRow("Username :", state.profile?.firstName ?: "—")
                DetailRow("Email Address :", state.profile?.email ?: "—")

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailColumn(
                        label = "Age :",
                        value = state.profile?.age?.toString() ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    DetailColumn(
                        label = "Height :",
                        value = state.profile?.heightCm?.let { "${it.toInt()} cm" } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailColumn(
                        label = "Gender :",
                        value = state.profile?.gender?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    DetailColumn(
                        label = "Lifestyle :",
                        value = state.profile?.activityLevel?.label ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(text = "Edit Details", onClick = onEditClick)

            Spacer(Modifier.height(12.dp))

            com.example.fithub.ui.components.SecondaryButton(
                text = "Settings",
                onClick = onSettingsClick
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.width(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}