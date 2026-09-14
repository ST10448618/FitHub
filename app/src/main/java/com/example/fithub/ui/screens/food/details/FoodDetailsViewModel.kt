package com.example.fithub.ui.screens.food.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class FoodDetailsUiState(
    val isLoading: Boolean = true,
    val food: Food? = null,
    val quantity: Int = 1,
    val mealType: MealType = MealType.BREAKFAST,
    val errorMessage: String? = null,
    val saved: Boolean = false,
// Calculated for the current selection
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
// Goal context
    val mealTarget: Int = 0,
    val mealConsumedToday: Double = 0.0
)

class FoodDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val foodId: String = savedStateHandle["foodId"] ?: ""
    private val initialQuantity: Int =
        savedStateHandle.get<String>("qty")?.toIntOrNull() ?: 1
    private val initialMealType: MealType =
        savedStateHandle.get<String>("meal")?.let {
            runCatching { MealType.valueOf(it) }.getOrNull()
        } ?: MealType.BREAKFAST

    private val foodRepo = ServiceLocator.foodRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val nutritionGoalsRepo = ServiceLocator.nutritionGoalsRepository

    private val _uiState = MutableStateFlow(
        FoodDetailsUiState(
            quantity = initialQuantity.coerceIn(1, 20),
            mealType = initialMealType
        )
    )
    val uiState: StateFlow<FoodDetailsUiState> = _uiState.asStateFlow()

    init {
        loadFood()
        observeMealContext()
    }

    private fun loadFood() {
        viewModelScope.launch {
            val food = foodRepo.getById(foodId)
            if (food == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Food not found.")
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false, food = food) }
            recalculate()
        }
    }

    private fun observeMealContext() {
        val today = LocalDate.now()
        viewModelScope.launch {
            foodLogRepo.observeByDate(uid, today).collect { logs ->
                val goals = nutritionGoalsRepo.getCurrent(uid)
                val consumedForMeal = logs
                    .filter { it.mealType == _uiState.value.mealType }
                    .sumOf { it.calories }
                val target = goals?.let {
                    when (_uiState.value.mealType) {
                        MealType.BREAKFAST -> it.mealTargets.breakfastKcal
                        MealType.LUNCH -> it.mealTargets.lunchKcal
                        MealType.DINNER -> it.mealTargets.dinnerKcal
                        MealType.SNACK -> it.mealTargets.snackKcal
                    }
                } ?: 0
                _uiState.update {
                    it.copy(mealTarget = target, mealConsumedToday = consumedForMeal)
                }
            }
        }
    }

    fun onQuantityChange(q: Int) {
        _uiState.update { it.copy(quantity = q.coerceIn(1, 20)) }
        recalculate()
    }

    fun onMealTypeChange(type: MealType) {
        _uiState.update { it.copy(mealType = type) }
        // Re-observe meal context for the new meal
        observeMealContext()
    }

    private fun recalculate() {
        val food = _uiState.value.food ?: return
        val grams = (food.servingSizeG ?: 100.0) * _uiState.value.quantity
        _uiState.update {
            it.copy(
                totalCalories = food.caloriesForGrams(grams),
                totalProtein = food.proteinForGrams(grams),
                totalCarbs = food.carbsForGrams(grams),
                totalFat = food.fatForGrams(grams)
            )
        }
    }

    fun addToLog() {
        val food = _uiState.value.food ?: return
        val state = _uiState.value
        val grams = (food.servingSizeG ?: 100.0) * state.quantity

        viewModelScope.launch {
            val log = FoodLog(
                id = IdGenerator.newId(),
                userId = uid,
                foodId = food.id,
                foodName = food.name,
                brandName = food.brand,
                imageUrl = food.imageUrl,
                mealType = state.mealType,
                source = food.source,
                barcode = if (food.id.startsWith("off:")) food.id.removePrefix("off:") else null,
                portionSize = state.quantity.toDouble(),
                portionUnit = food.servingLabel ?: "serving",
                portionGrams = grams,
                calories = state.totalCalories,
                proteinG = state.totalProtein,
                carbsG = state.totalCarbs,
                fatG = state.totalFat,
                logDate = LocalDate.now(),
                loggedAt = LocalDateTime.now()
            )
            when (val result = foodLogRepo.add(log)) {
                is Resource.Success -> _uiState.update { it.copy(saved = true) }
                is Resource.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }


}