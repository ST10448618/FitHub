package com.example.fithub.ui.screens.settings

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.BiometricHelper
import com.example.fithub.core.BiometricResult
import com.example.fithub.core.LogTags
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
    var showUnavailableDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshAvailability(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.settings_title), onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Push notifications (unchanged behaviour — Phase 4 wires FCM)
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

            // Language (unchanged — Phase 5 will replace the TODO)
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
                    // TODO(Phase 5): language picker dialog
                }
            )

            Spacer(Modifier.height(12.dp))

            // Fingerprint login (Phase 2: enable requires biometric success;
            // Firestore flag mirrors the local pref)
            SettingsRow(
                icon = { Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = FitHubPrimary) },
                title = stringResource(R.string.settings_fingerprint_title),
                subtitle = stringResource(
                    if (state.biometricEnabled) R.string.settings_fingerprint_subtitle_on
                    else R.string.settings_fingerprint_subtitle_off
                ),
                trailing = {
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = { shouldEnable ->
                            Log.i(LogTags.BIOMETRIC, "Settings: fingerprint toggle → $shouldEnable")
                            if (!shouldEnable) {
                                viewModel.setBiometric(false)
                                return@Switch
                            }
                            val activity = context as? FragmentActivity
                            if (activity == null) {
                                Log.w(LogTags.BIOMETRIC, "Settings: no FragmentActivity host")
                                return@Switch
                            }
                            when (BiometricHelper.canAuthenticate(context)) {
                                com.example.fithub.core.BiometricAvailability.Available -> {
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        titleRes = R.string.settings_fingerprint_enable_title,
                                        subtitleRes = R.string.settings_fingerprint_enable_subtitle,
                                        negativeRes = R.string.generic_cancel
                                    ) { result ->
                                        when (result) {
                                            BiometricResult.Success -> viewModel.setBiometric(true)
                                            is BiometricResult.Error -> {
                                                Log.w(LogTags.BIOMETRIC, "Settings: enable aborted (${result.kind}) — staying disabled")
                                            }
                                        }
                                    }
                                }
                                com.example.fithub.core.BiometricAvailability.NoneEnrolled,
                                com.example.fithub.core.BiometricAvailability.NoHardware,
                                com.example.fithub.core.BiometricAvailability.Unavailable ->
                                    showUnavailableDialog = true
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

    if (showUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showUnavailableDialog = false },
            title = { Text(stringResource(R.string.settings_fingerprint_unavailable_title)) },
            text = { Text(stringResource(R.string.settings_fingerprint_unavailable_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showUnavailableDialog = false
                    (context as? FragmentActivity)?.let { act ->
                        Log.i(LogTags.BIOMETRIC, "Settings: opening system biometric enrolment")
                        BiometricHelper.openBiometricSettings(act)
                    }
                }) { Text(stringResource(R.string.settings_fingerprint_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showUnavailableDialog = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
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
            ) { icon() }
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