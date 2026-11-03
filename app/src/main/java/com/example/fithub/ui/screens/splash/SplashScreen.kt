package com.example.fithub.ui.screens.splash

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.fithub.core.LogTags
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.theme.FitHubMidBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.FitHubSecondary
import kotlinx.coroutines.delay

/**
 * Two-second branded splash. Phase 1: [onFinished] no longer hard-codes the
 * next destination — the caller in [com.example.fithub.ui.navigation.FitHubNavGraph]
 * decides between [com.example.fithub.ui.navigation.Screen.BIOMETRIC_GATE] and
 * [com.example.fithub.ui.navigation.Screen.LANDING] based on biometric state.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var phase by remember { mutableIntStateOf(1) }

    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        Log.d(LogTags.APP, "SplashScreen visible")
        delay(1200)
        phase = 2
        delay(800)
        Log.d(LogTags.APP, "SplashScreen finished (phase=$phase)")
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FitHubLogo(
                modifier = Modifier.alpha(logoAlpha),
                iconSize = 200.dp
            )
        }
    }
}