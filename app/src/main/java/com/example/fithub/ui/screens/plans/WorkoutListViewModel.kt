package com.example.fithub.ui.screens.plans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutListUiState(
    val category: WorkoutCategory = WorkoutCategory.GENERAL_FITNESS,
    val isLoading: Boolean = true,
    val items: List<WorkoutPlan> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class WorkoutListViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryArg: String = savedStateHandle["category"] ?: "GENERAL_FITNESS"

    private val _uiState = MutableStateFlow(
        WorkoutListUiState(
            category = runCatching { WorkoutCategory.valueOf(categoryArg) }
                .getOrDefault(WorkoutCategory.GENERAL_FITNESS)
        )
    )
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    private val repo = ServiceLocator.workoutPlanRepository
    private var allPlans: List<WorkoutPlan> = emptyList()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plans = repo.getVerifiedByCategory(_uiState.value.category)
            allPlans = plans
            _uiState.update {
                it.copy(isLoading = false, items = filter(plans, it.searchQuery))
            }
        }
    }

    fun onSearchChange(q: String) {
        _uiState.update {
            it.copy(searchQuery = q, items = filter(allPlans, q))
        }
    }

    private fun filter(plans: List<WorkoutPlan>, q: String): List<WorkoutPlan> =
        if (q.isBlank()) plans
        else plans.filter { it.name.contains(q, ignoreCase = true) }
}