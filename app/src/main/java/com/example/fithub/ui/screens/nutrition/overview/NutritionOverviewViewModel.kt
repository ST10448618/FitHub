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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class NutritionOverviewUiState(
    val isLoading: Boolean = true,
    val monthAnchor: YearMonth = YearMonth.now(),
    val monthLabel: String = "",
    val goals: NutritionGoals? = null,
    val avgDailyIntake: Double = 0.0,
    val avgProtein: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgMealBreakdown: NutritionProgressCalculator.MealBreakdown =
        NutritionProgressCalculator.MealBreakdown()
)

class NutritionOverviewViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(NutritionOverviewUiState())
    val uiState: StateFlow<NutritionOverviewUiState> = _uiState.asStateFlow()

    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val nutritionRepo = ServiceLocator.nutritionGoalsRepository

    init { reload() }

    fun shiftMonth(months: Long) {
        _uiState.update { it.copy(monthAnchor = it.monthAnchor.plusMonths(months)) }
        reload()
    }

    private fun reload() {
        val anchor = _uiState.value.monthAnchor
        val start = anchor.atDay(1)
        val end = anchor.atEndOfMonth()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val goals = nutritionRepo.getCurrent(uid)
            val logs = foodLogRepo.getBetween(uid, start, end)
            val range = start..end

            val avg = NutritionProgressCalculator.averageDailyIntake(logs, range)
            val meals = NutritionProgressCalculator.averageMealBreakdown(logs, range)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    goals = goals,
                    monthLabel = anchor.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    avgDailyIntake = avg.calories,
                    avgProtein = avg.proteinG,
                    avgCarbs = avg.carbsG,
                    avgFat = avg.fatG,
                    avgMealBreakdown = meals
                )
            }
        }
    }
}