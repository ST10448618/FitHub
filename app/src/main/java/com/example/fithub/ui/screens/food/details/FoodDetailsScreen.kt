package com.example.fithub.ui.screens.food.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.domain.model.MealType
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.theme.*
import androidx.compose.runtime.setValue

@Composable
fun FoodDetailsScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: FoodDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onAdded()
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val food = state.food
    if (food == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage ?: "Food not found.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Hero image with back button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (food.imageUrl != null) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(FitHubMidBlue, FitHubPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽", style = MaterialTheme.typography.displayLarge)
                }
            }
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        // Details sheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                food.name,
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            if (!food.brand.isNullOrBlank()) {
                Text(food.brand, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            // Meal category + quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Meal Category : ",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                MealTypeChips(current = state.mealType, onSelect = viewModel::onMealTypeChange)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quantity :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = { viewModel.onQuantityChange(state.quantity - 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FitHubLightBlue)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    state.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = { viewModel.onQuantityChange(state.quantity + 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FitHubLightBlue)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Nutrition
            Text(
                "Nutritional Information",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${state.totalCalories.toInt()} cal",
                        style = MaterialTheme.typography.titleLarge,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        MacroBox("Protein", "${state.totalProtein.toInt()}g", MacroProtein, Modifier.weight(1f))
                        MacroBox("Carbs", "${state.totalCarbs.toInt()}g", MacroCarbs, Modifier.weight(1f))
                        MacroBox("Fats", "${state.totalFat.toInt()}g", MacroFats, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Meal target progress
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            state.mealType.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.Bold,
                            color = FitHubPrimary
                        )
                        Spacer(Modifier.weight(1f))
                        val used = state.mealConsumedToday + state.totalCalories
                        Text(
                            "${used.toInt()} / ${state.mealTarget} cal",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (state.mealTarget > 0)
                                ((state.mealConsumedToday + state.totalCalories) / state.mealTarget)
                                    .toFloat().coerceIn(0f, 1f)
                            else 0f
                        },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = FitHubPrimary,
                        trackColor = SurfaceGray
                    )
                }
            }

            if (food.description != null) {
                Spacer(Modifier.height(18.dp))
                Text(
                    "Description",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    food.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Add ${state.mealType.name.lowercase()
                        .replaceFirstChar { it.uppercase() }} Meal",
                    onClick = viewModel::addToLog,
                    modifier = Modifier.weight(1.4f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MealTypeChips(current: MealType, onSelect: (MealType) -> Unit) {
    var expanded by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                current.name.lowercase().replaceFirstChar { it.uppercase() },
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text("  ▾", color = FitHubPrimary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MealType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MacroBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}