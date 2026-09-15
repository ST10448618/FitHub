package com.example.fithub.ui.screens.nutrition.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.model.NutritionGoals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NutritionOverviewUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val dateLabel: String = "",
    val isToday: Boolean = true,
    val goals: NutritionGoals? = null,

    // Per-day actuals
    val consumedCalories: Double = 0.0,
    val consumedProtein: Double = 0.0,
    val consumedCarbs: Double = 0.0,
    val consumedFat: Double = 0.0,
    val mealBreakdown: NutritionProgressCalculator.MealBreakdown =
        NutritionProgressCalculator.MealBreakdown()
)

class NutritionOverviewViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(NutritionOverviewUiState())
    val uiState: StateFlow<NutritionOverviewUiState> = _uiState.asStateFlow()

    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val nutritionRepo = ServiceLocator.nutritionGoalsRepository

    init {
        observeDay(_uiState.value.selectedDate)
    }

    fun shiftDay(days: Long) {
        val newDate = _uiState.value.selectedDate.plusDays(days)
        if (newDate.isAfter(LocalDate.now())) return // don't go into the future
        observeDay(newDate)
    }

    private fun observeDay(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedDate = date,
                    dateLabel = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    isToday = date == LocalDate.now()
                )
            }

            // Goals (once)
            val goals = nutritionRepo.getCurrent(uid)
            _uiState.update { it.copy(goals = goals) }

            // Observe logs for the selected day
            foodLogRepo.observeByDate(uid, date).collect { logs ->
                val totals = NutritionProgressCalculator.dailyTotals(logs)
                val meals = NutritionProgressCalculator.mealBreakdown(logs)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        consumedCalories = totals.calories,
                        consumedProtein = totals.proteinG,
                        consumedCarbs = totals.carbsG,
                        consumedFat = totals.fatG,
                        mealBreakdown = meals
                    )
                }
            }
        }
    }
}