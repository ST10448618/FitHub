package com.example.fithub.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class CheckpointLoggerUiState(
    val isLoading: Boolean = true,
    val weight: Int = 70,
    val unit: String = "kg",

    // Progress context
    val previousWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val goalDirection: GoalDirection = GoalDirection.MAINTAIN,

    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

class CheckpointLoggerViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(CheckpointLoggerUiState())
    val uiState: StateFlow<CheckpointLoggerUiState> = _uiState.asStateFlow()

    private val weightRepo = ServiceLocator.weightRepository
    private val goalRepo = ServiceLocator.goalRepository

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val latest = weightRepo.getLatest(uid)
            val goal = goalRepo.getCurrent(uid)

            val initial = latest?.weightKg?.toInt() ?: 70
            _uiState.update {
                it.copy(
                    isLoading = false,
                    weight = initial,
                    previousWeightKg = latest?.weightKg,
                    targetWeightKg = goal?.targetWeightKg,
                    goalDirection = goal?.direction ?: GoalDirection.MAINTAIN
                )
            }
        }
    }

    fun onWeightChange(newWeight: Int) {
        _uiState.update { it.copy(weight = newWeight.coerceIn(20, 400)) }
    }

    fun save() {
        val state = _uiState.value
        val now = LocalDateTime.now()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val entry = WeightEntry(
                id = IdGenerator.newId(),
                userId = uid,
                weightKg = state.weight.toDouble(),
                date = LocalDate.now(),
                createdAt = now
            )

            when (val result = weightRepo.addEntry(entry)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saved = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    /** Progress text for the banner below the stepper. */
    fun progressMessage(): String? {
        val s = _uiState.value
        val previous = s.previousWeightKg ?: return null
        val target = s.targetWeightKg ?: return null
        val current = s.weight.toDouble()

        val deltaFromPrevious = current - previous
        if (kotlin.math.abs(deltaFromPrevious) < 0.1) return "You maintained your weight."

        val towardGoal = when (s.goalDirection) {
            GoalDirection.LOSE -> current < previous
            GoalDirection.GAIN -> current > previous
            GoalDirection.MAINTAIN -> kotlin.math.abs(current - target) <= 0.5
        }

        val delta = kotlin.math.abs(deltaFromPrevious).toInt()
        return when {
            towardGoal && s.goalDirection == GoalDirection.LOSE ->
                "You are now ${delta} kg closer to your goal!"
            towardGoal && s.goalDirection == GoalDirection.GAIN ->
                "You are now ${delta} kg closer to your goal!"
            towardGoal -> "On track — nice."
            else -> "You've moved ${delta} kg away from your goal."
        }
    }
}