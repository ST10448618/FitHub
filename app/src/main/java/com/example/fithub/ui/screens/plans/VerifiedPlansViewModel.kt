package com.example.fithub.ui.screens.plans

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

data class VerifiedPlansUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val allPlans: List<WorkoutPlan> = emptyList(),
    val plansByCategory: Map<WorkoutCategory, List<WorkoutPlan>> = emptyMap(),
    val errorMessage: String? = null
)

class VerifiedPlansViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VerifiedPlansUiState())
    val uiState: StateFlow<VerifiedPlansUiState> = _uiState.asStateFlow()

    private val repo = ServiceLocator.workoutPlanRepository

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            repo.observeVerified().collect { plans ->
                val filtered = filterByQuery(plans, _uiState.value.searchQuery)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allPlans = plans,
                        plansByCategory = groupByCategory(filtered)
                    )
                }
            }
        }
    }

    fun onSearchChange(q: String) {
        val filtered = filterByQuery(_uiState.value.allPlans, q)
        _uiState.update {
            it.copy(
                searchQuery = q,
                plansByCategory = groupByCategory(filtered)
            )
        }
    }

    private fun filterByQuery(plans: List<WorkoutPlan>, q: String): List<WorkoutPlan> {
        if (q.isBlank()) return plans
        return plans.filter { it.name.contains(q, ignoreCase = true) }
    }

    private fun groupByCategory(plans: List<WorkoutPlan>): Map<WorkoutCategory, List<WorkoutPlan>> =
        plans.groupBy { it.category }
}