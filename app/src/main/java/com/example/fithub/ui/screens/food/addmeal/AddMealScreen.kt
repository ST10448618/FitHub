package com.example.fithub.ui.screens.food.addmeal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.core.CategoryMapper
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.FoodCardHorizontal
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.theme.*

@Composable
fun AddMealScreen(
    onBack: () -> Unit,
    onFoodSelected: (String) -> Unit,      // passes foodId
    onBarcodeScan: () -> Unit,
    onCameraRecognition: () -> Unit,
    viewModel: AddMealViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Add Meal", onBack = onBack)

        // Search bar + scan button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search Meal or Food") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onCameraRecognition,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitHubPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("📸 Scan Meal", fontWeight = FontWeight.Bold)
            }
        }

        // Second scan button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            TextButton(onClick = onBarcodeScan) {
                Text("📷  Scan Barcode", color = FitHubPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Categories
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(CategoryMapper.FITHUB_CATEGORIES) { cat ->
                CategoryTile(
                    category = cat,
                    onClick = { viewModel.loadCategory(cat) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Search Results",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        when {
            state.isSearching -> LoadingState(message = "Searching…")
            state.errorMessage != null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.errorMessage!!, color = ErrorRed)
            }
            state.results.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Search for a food or pick a category.",
                    color = TextSecondary
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.results, key = { it.id }) { food ->
                    FoodCardHorizontal(
                        food = food,
                        onClick = { onFoodSelected(food.id) }
                    )
                }
            }
        }
    }
}