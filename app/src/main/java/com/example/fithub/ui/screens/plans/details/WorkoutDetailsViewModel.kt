package com.example.fithub.ui.screens.plans.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutDetailsUiState(
    val isLoading: Boolean = true,
    val plan: WorkoutPlan? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class WorkoutDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val planId: String = savedStateHandle["planId"] ?: ""

    private val _uiState = MutableStateFlow(WorkoutDetailsUiState())
    val uiState: StateFlow<WorkoutDetailsUiState> = _uiState.asStateFlow()

    private val repo = ServiceLocator.workoutPlanRepository

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val plan = repo.getById(planId)
            if (plan == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Workout plan not found.")
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false, plan = plan) }
        }
    }

    fun savePlan() {
        val plan = _uiState.value.plan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = repo.saveFromLibrary(uid, plan)) {
                is Resource.Success -> {
                    // Refresh
                    val refreshed = repo.getById(plan.id)
                    _uiState.update {
                        it.copy(isSaving = false, plan = refreshed ?: plan.copy(isSaved = true))
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun unsave() {
        val plan = _uiState.value.plan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = repo.unsave(uid, plan.id)) {
                is Resource.Success -> {
                    val refreshed = repo.getById(plan.id)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            plan = refreshed ?: plan.copy(isSaved = false)
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}