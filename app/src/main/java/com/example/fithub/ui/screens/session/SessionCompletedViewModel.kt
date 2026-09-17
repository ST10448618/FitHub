package com.example.fithub.ui.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import com.example.fithub.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SessionCompletedUiState(
    val isLoading: Boolean = true,
    val session: WorkoutSession? = null,
    val weeklyCompleted: Int = 0,
    val weeklyTarget: Int = 4,
    val weeklyProgressPercent: Int = 0,
    val particlesEarned: Int = 225,
    val errorMessage: String? = null
)

class SessionCompletedViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val sessionId: String = savedStateHandle["sessionId"] ?: ""

    private val _uiState = MutableStateFlow(SessionCompletedUiState())
    val uiState: StateFlow<SessionCompletedUiState> = _uiState.asStateFlow()

    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository

    init { load() }

    private fun load() {
        viewModelScope.launch {
            // Load all completed sessions and pick out the one we just created
            val sessions = sessionRepo.getBetween(uid, LocalDate.now().minusDays(30), LocalDate.now())
            val session = sessions.firstOrNull { it.id == sessionId }
            if (session == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Session not found.")
                }
                return@launch
            }

            val goals = workoutGoalsRepo.get(uid)
            val target = goals?.sessionsPerWeek ?: 4

            val allCompleted = sessions.filter { it.status == com.example.fithub.domain.model.SessionStatus.COMPLETED }
            val thisWeek = WorkoutProgressCalculator
                .sessionsThisWeek(allCompleted, LocalDate.now()).size

            val particles = (session.estimatedActivityKcal * 0.7).toInt().coerceIn(50, 500)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    session = session,
                    weeklyCompleted = thisWeek,
                    weeklyTarget = target,
                    weeklyProgressPercent =
                        if (target > 0) ((thisWeek.toFloat() / target) * 100).toInt().coerceIn(0, 100)
                        else 0,
                    particlesEarned = particles
                )
            }
        }
    }
}