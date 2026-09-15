package com.example.fithub.ui.screens.goals.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.model.Goal
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class WeightGoalUiState(
    val isLoading: Boolean = true,
    val currentWeightKg: Int? = null,
    val targetWeightKg: Int = 70,
    val direction: GoalDirection = GoalDirection.MAINTAIN,
    val differenceKg: Int = 0,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val errorMessage: String? = null
)

class WeightGoalViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(WeightGoalUiState())
    val uiState: StateFlow<WeightGoalUiState> = _uiState.asStateFlow()

    private val weightRepo = ServiceLocator.weightRepository
    private val goalRepo = ServiceLocator.goalRepository

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // getLatest expects the USER ID, not a record ID
            val latest = weightRepo.getLatest(uid)
            val current = latest?.weightKg?.toInt()
            val existingGoal = goalRepo.getCurrent(uid)

            val target = existingGoal?.targetWeightKg?.toInt() ?: current ?: 70

            recompute(current, target)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentWeightKg = current,
                    targetWeightKg = target
                )
            }
        }
    }

    fun onTargetChange(newTarget: Int) {
        val current = _uiState.value.currentWeightKg
        recompute(current, newTarget)
        _uiState.update { it.copy(targetWeightKg = newTarget, errorMessage = null) }
    }

    private fun recompute(current: Int?, target: Int) {
        if (current == null) return
        val direction = GoalDirectionCalculator.from(current.toDouble(), target.toDouble())
        val diff = (target - current)
        _uiState.update {
            it.copy(
                direction = direction,
                differenceKg = diff
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val current = state.currentWeightKg
        if (current == null) {
            _uiState.update { it.copy(errorMessage = "Log a weigh-in first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val goal = Goal(
                id = IdGenerator.userSingletonId("goal", uid),   // stable ID — correct
                userId = uid,
                currentWeightKgAtGoalSet = current.toDouble(),
                targetWeightKg = state.targetWeightKg.toDouble(),
                direction = state.direction,
                effectiveDate = LocalDate.now(),
                createdAt = LocalDateTime.now()
            )

            when (val result = goalRepo.setGoal(goal)) {
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