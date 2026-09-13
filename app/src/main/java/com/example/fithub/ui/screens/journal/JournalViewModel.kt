package com.example.fithub.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class JournalViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val weightRepo = ServiceLocator.weightRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val checkpointRepo = ServiceLocator.checkpointRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository

    init {
        observeWeight()
        observeGoal()
        observeCheckpoint()
        observeNutrition()
        observeWorkouts()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        observeNutrition()
        observeWorkouts()
    }

    private fun observeWeight() {
        viewModelScope.launch {
            weightRepo.observeLatest(uid).collect { w ->
                _uiState.update { it.copy(latestWeight = w, isLoading = false) }
            }
        }
    }

    private fun observeGoal() {
        viewModelScope.launch {
            goalRepo.observeCurrent(uid).collect { goal ->
                _uiState.update { it.copy(targetWeightKg = goal?.targetWeightKg) }
            }
        }
    }

    private fun observeCheckpoint() {
        viewModelScope.launch {
            checkpointRepo.observe(uid).collect { schedule ->
                _uiState.update { it.copy(nextCheckpointDate = schedule?.nextCheckpointDate) }
            }
        }
    }

    private fun observeNutrition() {
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            foodLogRepo.observeByDate(uid, date).collect { logs ->
                _uiState.update { it.copy(foodLogs = logs) }
            }
        }
    }

    private fun observeWorkouts() {
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            sessionRepo.observeByDate(uid, date).collect { sessions ->
                _uiState.update { it.copy(workoutSessions = sessions) }
            }
        }
    }
}