package com.example.fithub.ui.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import com.example.fithub.domain.model.WorkoutGoals
import com.example.fithub.domain.model.WorkoutPlan
import com.example.fithub.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlansUiState(
    val isLoading: Boolean = true,

    // Today's activities
    val todayActivityKcal: Int = 0,
    val todayActiveMinutes: Int = 0,
    val workoutStreakDays: Int = 0,

    // Weekly goal
    val weeklyGoalTarget: Int = 4,
    val weeklyGoalCompleted: Int = 0,
    val weeklyGoalPercent: Int = 0,

    // Sections
    val recentSessions: List<WorkoutSession> = emptyList(),
    val createdPlans: List<WorkoutPlan> = emptyList(),
    val savedPlans: List<WorkoutPlan> = emptyList(),

    val errorMessage: String? = null
)

class PlansViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val planRepo = ServiceLocator.workoutPlanRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository

    init {
        observeSessions()
        observePlans()
        observeGoals()
    }

    private fun observeSessions() {
        val today = LocalDate.now()
        viewModelScope.launch {
            sessionRepo.observeCompleted(uid).collect { sessions ->
                val todaySessions = sessions.filter { it.logDate == today }
                val todayKcal = todaySessions.sumOf { it.estimatedActivityKcal }
                val todayMinutes = todaySessions.sumOf { it.durationMinutes }

                val target = _uiState.value.weeklyGoalTarget
                val thisWeekCount = WorkoutProgressCalculator
                    .sessionsThisWeek(sessions, today).size

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayActivityKcal = todayKcal,
                        todayActiveMinutes = todayMinutes,
                        workoutStreakDays = calculateStreak(sessions, today),
                        recentSessions = sessions.take(10),
                        weeklyGoalCompleted = thisWeekCount,
                        weeklyGoalPercent =
                            if (target > 0) ((thisWeekCount.toFloat() / target) * 100).toInt()
                                .coerceIn(0, 100)
                            else 0
                    )
                }
            }
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            planRepo.observeCreated(uid).collect { plans ->
                _uiState.update { it.copy(createdPlans = plans) }
            }
        }
        viewModelScope.launch {
            planRepo.observeSaved(uid).collect { plans ->
                _uiState.update { it.copy(savedPlans = plans) }
            }
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            workoutGoalsRepo.observe(uid).collect { goals ->
                val target = goals?.sessionsPerWeek ?: 4
                val completed = _uiState.value.weeklyGoalCompleted
                _uiState.update {
                    it.copy(
                        weeklyGoalTarget = target,
                        weeklyGoalPercent =
                            ((completed.toFloat() / target) * 100).toInt().coerceIn(0, 100)
                    )
                }
            }
        }
    }

    /** Consecutive days with at least one completed session, ending today or yesterday. */
    private fun calculateStreak(sessions: List<WorkoutSession>, today: LocalDate): Int {
        val daysWithSession = sessions.map { it.logDate }.toSet()
        if (daysWithSession.isEmpty()) return 0

        var streak = 0
        var cursor = if (daysWithSession.contains(today)) today else today.minusDays(1)
        while (daysWithSession.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}