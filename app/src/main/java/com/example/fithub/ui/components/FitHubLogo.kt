package com.example.fithub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fithub.R

@Composable
fun FitHubLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 120.dp,
    showWordmark: Boolean = true
) {
    Image(
        painter = painterResource(id = R.drawable.fithub_logo),
        contentDescription = "FitHub",
        modifier = modifier.size(iconSize),
        contentScale = ContentScale.Fit
    )
}
