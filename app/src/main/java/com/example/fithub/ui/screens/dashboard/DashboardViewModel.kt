package com.example.fithub.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel : ViewModel() {

    private val uid: String = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val weightRepo = ServiceLocator.weightRepository
    private val nutritionGoalsRepo = ServiceLocator.nutritionGoalsRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val planRepo = ServiceLocator.workoutPlanRepository

    init {
        observeProfile()
        observeGoalsAndWeight()
        observeNutrition()
        observeWorkouts()
        loadRecommendations()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    private fun observeGoalsAndWeight() {
        viewModelScope.launch {
            goalRepo.observeCurrent(uid).collect { goal ->
                _uiState.update { it.copy(targetWeightKg = goal?.targetWeightKg) }
            }
        }
        viewModelScope.launch {
            weightRepo.observeLatest(uid).collect { w ->
                _uiState.update { it.copy(currentWeightKg = w?.weightKg) }
            }
        }
    }

    private fun observeNutrition() {
        val today = LocalDate.now()
        viewModelScope.launch {
            // Ensure NutritionGoals exist — create default if missing
            var goals = nutritionGoalsRepo.getCurrent(uid)
            if (goals == null) {
                val profile = userRepo.getProfile(uid)
                val goal = goalRepo.getCurrent(uid)
                val direction = goal?.direction ?: com.example.fithub.domain.model.GoalDirection.MAINTAIN

                val recommended = if (profile != null) {
                    com.example.fithub.domain.calculator.CalorieEngine.calculate(
                        com.example.fithub.domain.calculator.CalorieEngine.Input(
                            age = profile.age,
                            gender = profile.gender,
                            heightCm = profile.heightCm,
                            weightKg = profile.currentWeightKg,
                            activityLevel = profile.activityLevel,
                            goalDirection = direction
                        )
                    ).recommendedDailyCalories
                } else 2000

                goals = com.example.fithub.domain.model.NutritionGoals(
                    id = com.example.fithub.util.IdGenerator.newId(),
                    userId = uid,
                    recommendedDailyCalories = recommended,
                    userDailyCalories = recommended,
                    mealTargets = com.example.fithub.domain.calculator.MacroCalculator
                        .starterMealTargets(recommended),
                    macroTargets = com.example.fithub.domain.calculator.MacroCalculator
                        .starterMacros(recommended)
                )
                nutritionGoalsRepo.save(goals)
            }

            val finalGoals = goals!!

            foodLogRepo.observeByDate(uid, today).collect { logs ->
                val snapshot = NutritionProgressCalculator.dashboardSnapshot(
                    todayLogs = logs,
                    mealTargets = finalGoals.mealTargets,
                    dailyCalorieTarget = finalGoals.userDailyCalories,
                    macroTargets = finalGoals.macroTargets
                )
                _uiState.update { it.copy(nutritionSnapshot = snapshot) }
            }
        }
    }

    private fun observeWorkouts() {
        val today = LocalDate.now()
        viewModelScope.launch {
            sessionRepo.observeCompleted(uid).collect { sessions ->
                val workoutGoals = workoutGoalsRepo.get(uid)
                val target = workoutGoals?.sessionsPerWeek ?: 4
                val thisWeek = WorkoutProgressCalculator.sessionsThisWeek(sessions, today)
                _uiState.update {
                    it.copy(
                        recentSessions = sessions.take(5),
                        weeklyWorkoutCompleted = thisWeek.size,
                        weeklyWorkoutTarget = target,
                        weeklyWorkoutProgressPercent = WorkoutProgressCalculator
                            .weeklyProgress(sessions, today, target)
                    )
                }
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            // TODO(Track B): real recommendations based on goal/history
            val verified = mutableListOf<com.example.fithub.domain.model.WorkoutPlan>()
            // Fallback: pull from repo (first few verified plans)
            ServiceLocator.workoutPlanRepository
                .observeVerified()
                .collect { plans ->
                    _uiState.update { it.copy(recommendedPlans = plans.take(4)) }
                }
        }
    }
}