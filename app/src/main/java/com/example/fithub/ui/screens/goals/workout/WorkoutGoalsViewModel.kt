package com.example.fithub.ui.screens.goals.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.WorkoutGoals
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class WorkoutGoalsUiState(
    val isLoading: Boolean = true,
    val sessionsPerWeek: Int = 4,
    val monthlyActivityGoalKcal: Int = 10000,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val errorMessage: String? = null
)

class WorkoutGoalsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(WorkoutGoalsUiState())
    val uiState: StateFlow<WorkoutGoalsUiState> = _uiState.asStateFlow()

    private val repo = ServiceLocator.workoutGoalsRepository

    init {
        viewModelScope.launch {
            val existing = repo.get(uid)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sessionsPerWeek = existing?.sessionsPerWeek ?: 4,
                    monthlyActivityGoalKcal = existing?.monthlyActivityGoalKcal ?: 10000
                )
            }
        }
    }

    fun onSessionsChange(v: Int) = _uiState.update {
        it.copy(sessionsPerWeek = v.coerceIn(1, 7), errorMessage = null)
    }

    fun onMonthlyChange(v: Int) = _uiState.update {
        it.copy(monthlyActivityGoalKcal = v.coerceAtLeast(500), errorMessage = null)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val goals = WorkoutGoals(
                id = IdGenerator.newId(),
                userId = uid,
                sessionsPerWeek = state.sessionsPerWeek,
                monthlyActivityGoalKcal = state.monthlyActivityGoalKcal,
                updatedAt = LocalDateTime.now()
            )
            when (val result = repo.save(goals)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saveComplete = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}