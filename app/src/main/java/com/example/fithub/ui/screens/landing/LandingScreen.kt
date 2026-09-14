package com.example.fithub.ui.screens.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.theme.FitHubMidBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.FitHubSecondary
import androidx.compose.ui.draw.alpha

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            FitHubLogo(
                iconSize = 200.dp
            )

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "LOGIN",
                onClick = onLoginClick
            )

            Spacer(Modifier.height(14.dp))

            SecondaryButton(
                text = "REGISTER",
                onClick = onRegisterClick,
                borderColor = Color.White,
                contentColor = Color.White
            )
        }
    }
}