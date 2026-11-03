package com.example.fithub.ui.screens.settings

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.BiometricHelper
import com.example.fithub.core.BiometricResult
import com.example.fithub.core.LogTags
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SmallActionButton
import com.example.fithub.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showUnavailableDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotifications(true)
        } else {
            viewModel.toggleNotifications(false)
        }
    }

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

            // Push notifications
            SettingsRow(
                icon = { Icon(Icons.Filled.Notifications, contentDescription = null, tint = FitHubPrimary) },
                title = "Push Notifications",
                subtitle = "Current : ${if (state.pushNotifications) "ENABLED" else "DISABLED"}",
                trailing = {
                    Switch(
                        checked = state.pushNotifications,
                        onCheckedChange = { shouldEnable ->
                            if (shouldEnable) {
                                if (Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleNotifications(true)
                                }
                            } else {
                                viewModel.toggleNotifications(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FitHubPrimary,
                            checkedTrackColor = FitHubLightBlue
                        )
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            // Language (Phase 5 will implement)
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
                onClick = { /* TODO(Phase 5) */ }
            )

            Spacer(Modifier.height(12.dp))

            // Fingerprint login
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
                            if (activity == null) return@Switch

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
                                            is BiometricResult.Error -> Log.w(LogTags.BIOMETRIC, "Enable aborted")
                                        }
                                    }
                                }
                                else -> showUnavailableDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FitHubPrimary,
                            checkedTrackColor = FitHubLightBlue
                        )
                    )
                }
            )

            Spacer(Modifier.height(14.dp))

            // Phase 4: Diagnostics Card
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.notif_diag_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val token = state.fcmToken
                            if (!token.isNullOrBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FCM Token", token))
                                Toast.makeText(context, R.string.notif_diag_token_copied, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, R.string.notif_diag_token_missing, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.notif_diag_token_label) + ": " + (state.fcmToken?.take(8) ?: "—") + "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.notif_diag_token_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = FitHubPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                SmallActionButton(
                    text = stringResource(R.string.notif_diag_send_test),
                    onClick = { viewModel.sendTestNotification(context) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    (context as? FragmentActivity)?.let { BiometricHelper.openBiometricSettings(it) }
                }) { Text(stringResource(R.string.settings_fingerprint_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showUnavailableDialog = false }) { Text(stringResource(R.string.generic_cancel)) }
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
                Text(title, style = MaterialTheme.typography.titleSmall, color = FitHubPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            trailing()
        }
    }
}