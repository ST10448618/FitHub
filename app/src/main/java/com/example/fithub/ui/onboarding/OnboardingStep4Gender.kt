package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.core.OnboardingSession
import com.example.fithub.domain.model.Gender
import com.example.fithub.ui.theme.*

@Composable
fun OnboardingStep4Gender(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var selected by remember { mutableStateOf(OnboardingSession.gender) }

    OnboardingScaffold(
        stepNumber = 4,
        title = "What do you identify as?",
        subtitle = "There wasn't enough space to fit all of them so please pick from two.",
        onCancel = onCancel,
        onNext = {
            OnboardingSession.gender = selected
            onNext()
        },
        nextEnabled = selected != null
    ) {
        Text(
            "What gender are you?",
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderCard(
                label = "Male",
                symbol = "♂",
                selected = selected == Gender.MALE,
                color = FitHubPrimary,
                onClick = { selected = Gender.MALE },
                modifier = Modifier.weight(1f)
            )
            GenderCard(
                label = "Female",
                symbol = "♀",
                selected = selected == Gender.FEMALE,
                color = Color(0xFFE91E63),
                onClick = { selected = Gender.FEMALE },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}

@Composable
private fun GenderCard(
    label: String,
    symbol: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) color else Color.LightGray,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                symbol,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) color else TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}