package com.example.fithub.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(listOf(FitHubMidBlue, FitHubPrimary))
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

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Your Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FitHubPrimary, RoundedCornerShape(10.dp))
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

            PrimaryButton(
                text = stringResource(R.string.profile_edit_details),
                onClick = onEditClick
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = stringResource(R.string.profile_settings),
                onClick = onSettingsClick
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = stringResource(R.string.profile_logout),
                onClick = { showLogoutDialog = true },
                borderColor = ErrorRed,
                contentColor = ErrorRed
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    Log.i(LogTags.AUTH, "ProfileScreen: logout confirmed")
                    scope.launch {
                        when (val result = ServiceLocator.authRepository.logout()) {
                            is com.example.fithub.core.Resource.Error ->
                                Log.e(LogTags.AUTH, "Logout failed: ${result.message}")
                            is com.example.fithub.core.Resource.Success ->
                                Log.i(LogTags.AUTH, "Logout succeeded")
                            com.example.fithub.core.Resource.Loading -> Unit
                        }
                        onLogout()
                    }
                }) { Text(stringResource(R.string.profile_logout_confirm_yes), color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
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