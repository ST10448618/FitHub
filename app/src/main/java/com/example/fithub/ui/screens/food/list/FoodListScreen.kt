package com.example.fithub.ui.screens.food.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.ErrorState
import com.example.fithub.ui.components.FoodCardHorizontal
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.theme.*

@Composable
fun FoodListScreen(
    onBack: () -> Unit,
    onFoodSelected: (String) -> Unit,
    viewModel: FoodListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(
            title = "Food · ${state.category}",
            onBack = onBack
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.load() }
                )
                state.items.isEmpty() -> EmptyState(
                    title = "No foods found",
                    message = "Try another category or search from Add Meal.",
                    emoji = "🥣"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { food ->
                        FoodCardHorizontal(
                            food = food,
                            onClick = { onFoodSelected(food.id) }
                        )
                    }
                }
            }
        }
    }
}