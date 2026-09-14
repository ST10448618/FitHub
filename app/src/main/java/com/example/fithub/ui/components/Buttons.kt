package com.example.fithub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fithub.ui.theme.*

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    val baseModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    Button(
        onClick = onClick,
        modifier = baseModifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FitHubPrimary,
            contentColor = Color.White,
            disabledContainerColor = FitHubPrimary.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    val baseModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    Box(
        modifier = baseModifier
            .height(52.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (!enabled) Modifier.background(Color.Gray.copy(alpha = 0.3f)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    borderColor: Color = FitHubPrimary,
    contentColor: Color = FitHubPrimary
) {
    val baseModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    OutlinedButton(
        onClick = onClick,
        modifier = baseModifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TextOnlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FitHubPrimary
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = FitHubPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}



