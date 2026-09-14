package com.example.fithub.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.core.BiometricHelper
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Push Notifications
            SettingsRow(
                icon = { Icon(Icons.Filled.Notifications, contentDescription = null, tint = FitHubPrimary) },
                title = "Push Notifications",
                subtitle = "Current : ${if (state.pushNotifications) "ENABLED" else "DISABLED"}",
                trailing = {
                    Switch(
                        checked = state.pushNotifications,
                        onCheckedChange = viewModel::toggleNotifications,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FitHubPrimary,
                            checkedTrackColor = FitHubLightBlue
                        )
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            // Language
            SettingsRow(
                icon = { Icon(Icons.Filled.Language, contentDescription = null, tint = FitHubPrimary) },
                title = "Language",
                subtitle = "Current : ${state.language}",
                trailing = {
                    Text(
                        "Change >",
                        style = MaterialTheme.typography.labelLarge,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = {
                    // TODO(Track A): language picker dialog
                }
            )

            Spacer(Modifier.height(12.dp))

            // Fingerprint Login
            SettingsRow(
                icon = { Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = FitHubPrimary) },
                title = "Fingerprint Login",
                subtitle = "Current : ${if (state.biometricEnabled) "ENABLED" else "DISABLED"}",
                trailing = {
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = { shouldEnable ->
                            if (shouldEnable) {
                                val activity = context as? FragmentActivity
                                if (activity != null && BiometricHelper.isAvailable(context)) {
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        title = "Enable Fingerprint Login",
                                        subtitle = "Confirm your identity",
                                        onSuccess = { viewModel.setBiometric(true) },
                                        onError = { viewModel.setBiometric(false) }
                                    )
                                } else {
                                    viewModel.setBiometric(false)
                                }
                            } else {
                                viewModel.setBiometric(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FitHubPrimary,
                            checkedTrackColor = FitHubLightBlue
                        )
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    RoundedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FitHubLightBlue),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            trailing()
        }
    }
}