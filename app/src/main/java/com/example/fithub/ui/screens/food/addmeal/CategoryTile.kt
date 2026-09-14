package com.example.fithub.ui.screens.food.addmeal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fithub.core.CategoryMapper
import com.example.fithub.ui.theme.CardWhite
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun CategoryTile(
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(
        CategoryMapper.drawableNameFor(category),
        "drawable",
        context.packageName
    )
    val hasImage = resId != 0

    Column(
        modifier = modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = resId,
                    contentDescription = category,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = CategoryMapper.emojiFor(category),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}