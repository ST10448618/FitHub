package com.example.fithub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fithub.R
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.ui.theme.FitHubLightBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SuccessGreen
import com.example.fithub.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@Composable
fun SyncStatusBanner(
    modifier: Modifier = Modifier
) {
    val uid = remember { SessionManager.currentUserId }
    val pendingCountFlow = remember(uid) {
        if (uid == null) flowOf(0)
        else ServiceLocator.syncManager.observePendingCount(uid)
    }
    val pending by pendingCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val online by ServiceLocator.connectivityMonitor.isOnline.collectAsStateWithLifecycle()

    var showSyncedTick by remember { mutableStateOf(false) }
    val previousPending = remember { MutableStateFlow(pending) }
    LaunchedEffect(pending, online) {
        val prev = previousPending.value
        previousPending.value = pending
        if (online && prev > 0 && pending == 0) {
            showSyncedTick = true
            delay(2_500)
            showSyncedTick = false
        }
    }

    val visible = !online || pending > 0 || showSyncedTick
    if (!visible) return

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(180)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        !online -> TextSecondary.copy(alpha = 0.15f)
                        pending > 0 -> FitHubLightBlue.copy(alpha = 0.6f)
                        else -> SuccessGreen.copy(alpha = 0.15f)
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when {
                    !online -> stringResource(R.string.sync_banner_offline)
                    pending > 0 -> stringResource(R.string.sync_banner_syncing, pending)
                    else -> stringResource(R.string.sync_banner_done)
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    !online -> TextSecondary
                    pending > 0 -> FitHubPrimary
                    else -> SuccessGreen
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}