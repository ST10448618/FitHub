package com.example.fithub.ui.screens.food.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FoodListUiState(
    val category: String = "",
    val isLoading: Boolean = true,
    val items: List<Food> = emptyList(),
    val errorMessage: String? = null
)

class FoodListViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryArg: String = savedStateHandle["category"] ?: "All"

    private val _uiState = MutableStateFlow(FoodListUiState(category = categoryArg))
    val uiState: StateFlow<FoodListUiState> = _uiState.asStateFlow()

    private val foodRepo = ServiceLocator.foodRepository

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (categoryArg.equals("all", ignoreCase = true)) {
                // Show everything in the local cache
                Resource.Success(
                    ServiceLocator.database.foodDao()
                        .getAllCached()
                        .map { entity -> entityToDomain(entity) }
                )
            } else {
                foodRepo.getByCategory(categoryArg)
            }

            when (result) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, items = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    // Helper to convert entity → domain (move the private method out of the repo)
    private fun entityToDomain(e: com.example.fithub.data.local.entity.FoodEntity) =
        com.example.fithub.domain.model.Food(
            id = e.id, name = e.name, brand = e.brand, imageUrl = e.imageUrl,
            category = e.category, caloriesPer100g = e.caloriesPer100g,
            proteinPer100g = e.proteinPer100g, carbsPer100g = e.carbsPer100g,
            fatPer100g = e.fatPer100g, fiberPer100g = e.fiberPer100g,
            sugarPer100g = e.sugarPer100g, sodiumPer100g = e.sodiumPer100g,
            servingSizeG = e.servingSizeG, servingLabel = e.servingLabel,
            source = e.source, description = e.description
        )

}