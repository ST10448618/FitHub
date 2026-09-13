package com.example.fithub.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalsTargetsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(GoalsTargetsUiState())
    val uiState: StateFlow<GoalsTargetsUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val weightRepo = ServiceLocator.weightRepository
    private val nutritionGoalsRepo = ServiceLocator.nutritionGoalsRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository
    private val checkpointRepo = ServiceLocator.checkpointRepository

    init {
        observeProfile()
        observeGoal()
        observeWeight()
        observeNutritionGoals()
        observeWorkoutGoals()
        observeCheckpoints()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update {
                    it.copy(startingWeightKg = profile?.startingWeightKg, isLoading = false)
                }
            }
        }
    }

    private fun observeGoal() {
        viewModelScope.launch {
            goalRepo.observeCurrent(uid).collect { goal ->
                _uiState.update { it.copy(goal = goal) }
            }
        }
    }

    private fun observeWeight() {
        viewModelScope.launch {
            weightRepo.observeLatest(uid).collect { w ->
                _uiState.update { it.copy(currentWeightKg = w?.weightKg) }
            }
        }
    }

    private fun observeNutritionGoals() {
        viewModelScope.launch {
            nutritionGoalsRepo.observeCurrent(uid).collect { goals ->
                _uiState.update { it.copy(nutritionGoals = goals) }
            }
        }
    }

    private fun observeWorkoutGoals() {
        viewModelScope.launch {
            workoutGoalsRepo.observe(uid).collect { goals ->
                _uiState.update { it.copy(workoutGoals = goals) }
            }
        }
    }

    private fun observeCheckpoints() {
        viewModelScope.launch {
            checkpointRepo.observe(uid).collect { schedule ->
                _uiState.update { it.copy(nextCheckpointDate = schedule?.nextCheckpointDate) }
            }
        }
    }
}