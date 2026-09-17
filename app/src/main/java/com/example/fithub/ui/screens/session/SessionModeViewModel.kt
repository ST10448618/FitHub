package com.example.fithub.ui.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class SessionModeUiState(
    val isLoading: Boolean = true,
    val plan: WorkoutPlan? = null,

    // Current position
    val currentIndex: Int = 0,
    val currentExercise: WorkoutExercise? = null,
    val currentIsTimeBased: Boolean = false,
    val secondsRemaining: Int = 0,
    val totalSecondsForCurrent: Int = 0,

    // Session lifecycle
    val isPaused: Boolean = false,
    val isCompleting: Boolean = false,
    val sessionStartTime: LocalDateTime = LocalDateTime.now(),

    // Navigation triggers
    val navigateToCompletedSessionId: String? = null,
    val navigateBack: Boolean = false,
    val errorMessage: String? = null
)

class SessionModeViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val planId: String = savedStateHandle["planId"] ?: ""

    private val _uiState = MutableStateFlow(SessionModeUiState())
    val uiState: StateFlow<SessionModeUiState> = _uiState.asStateFlow()

    private val planRepo = ServiceLocator.workoutPlanRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val rewardRepo = ServiceLocator.rewardRepository

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val plan = planRepo.getById(planId)
            if (plan == null || plan.exercises.isEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Workout not found.")
                }
                return@launch
            }
            val sorted = plan.exercises.sortedBy { it.orderIndex }
            val sortedPlan = plan.copy(exercises = sorted)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    plan = sortedPlan,
                    sessionStartTime = LocalDateTime.now(),
                    currentIndex = 0
                )
            }
            loadExercise(0)
        }
    }

    private fun loadExercise(index: Int) {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (index !in plan.exercises.indices) return

        val ex = plan.exercises[index]
        // Determine if time-based from the seeded catalog
        viewModelScope.launch {
            val catalog = ServiceLocator.exerciseRepository.getById(ex.exerciseId)
            val isTimeBased = catalog?.isTimeBased == true

            val totalSeconds = if (isTimeBased) {
                // use configured duration
                ex.targetDurationSeconds.takeIf { it > 0 } ?: 60
            } else {
                // rep-based → approximate seconds (3s per rep)
                val perRep = catalog?.estimatedSecondsPerRep ?: 3
                (ex.targetReps * perRep).coerceAtLeast(15)
            }

            _uiState.update {
                it.copy(
                    currentIndex = index,
                    currentExercise = ex,
                    currentIsTimeBased = isTimeBased,
                    secondsRemaining = totalSeconds,
                    totalSecondsForCurrent = totalSeconds
                )
            }
        }
    }

    /** Called every second by the screen while the session is active. */
    fun onTimerTick() {
        val s = _uiState.value
        if (s.isPaused || s.isCompleting) return
        if (s.secondsRemaining <= 0) return

        val next = s.secondsRemaining - 1
        _uiState.update { it.copy(secondsRemaining = next) }

        if (next == 0) {
            // Auto-advance or complete
            val plan = s.plan ?: return
            if (s.currentIndex < plan.exercises.lastIndex) {
                loadExercise(s.currentIndex + 1)
            } else {
                completeSession()
            }
        }
    }

    fun next() {
        val s = _uiState.value
        val plan = s.plan ?: return
        if (s.currentIndex < plan.exercises.lastIndex) {
            loadExercise(s.currentIndex + 1)
        } else {
            completeSession()
        }
    }

    fun previous() {
        val s = _uiState.value
        if (s.currentIndex > 0) loadExercise(s.currentIndex - 1)
    }

    fun pause() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resume() {
        _uiState.update { it.copy(isPaused = false) }
    }

    /** Saves the session as PARTIAL. Doesn't count toward weekly goal. */
    fun saveAndExit() {
        val s = _uiState.value
        val plan = s.plan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            val completed = completedExercisesFor(s)
            val durationMin = elapsedMinutes(s)
            val activity = completed.sumOf { it.estimatedActivityKcal }.toInt()

            val session = WorkoutSession(
                id = IdGenerator.newId(),
                userId = uid,
                workoutPlanId = plan.id,
                workoutName = plan.name,
                workoutCategory = plan.category,
                exercises = completed,
                durationMinutes = durationMin,
                estimatedActivityKcal = activity,
                status = SessionStatus.PARTIAL,
                startedAt = s.sessionStartTime,
                completedAt = LocalDateTime.now(),
                logDate = LocalDate.now()
            )

            sessionRepo.save(session)
            _uiState.update { it.copy(isCompleting = false, navigateBack = true) }
        }
    }

    /** Discards without saving. */
    fun discardAndExit() {
        _uiState.update { it.copy(navigateBack = true) }
    }

    private fun completeSession() {
        val s = _uiState.value
        val plan = s.plan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            val completed = completedExercisesFor(s, forceAll = true)
            val durationMin = elapsedMinutes(s)
            val activity = completed.sumOf { it.estimatedActivityKcal }.toInt()

            val session = WorkoutSession(
                id = IdGenerator.newId(),
                userId = uid,
                workoutPlanId = plan.id,
                workoutName = plan.name,
                workoutCategory = plan.category,
                exercises = completed,
                durationMinutes = durationMin,
                estimatedActivityKcal = activity,
                status = SessionStatus.COMPLETED,
                startedAt = s.sessionStartTime,
                completedAt = LocalDateTime.now(),
                logDate = LocalDate.now()
            )

            when (val saveResult = sessionRepo.save(session)) {
                is Resource.Success -> {
                    val particles = particlesEarned(activity)
                    rewardRepo.addParticles(uid, particles)
                    _uiState.update {
                        it.copy(
                            isCompleting = false,
                            navigateToCompletedSessionId = session.id
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isCompleting = false, errorMessage = saveResult.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun completedExercisesFor(
        state: SessionModeUiState,
        forceAll: Boolean = false
    ): List<CompletedExercise> {
        val plan = state.plan ?: return emptyList()
        val upto = if (forceAll) plan.exercises.lastIndex else state.currentIndex
        return plan.exercises.take(upto + 1).map { ex ->
            CompletedExercise(
                exerciseId = ex.exerciseId,
                exerciseName = ex.exerciseName,
                reps = ex.targetReps,
                durationSeconds = ex.targetDurationSeconds,
                estimatedActivityKcal = ex.estimatedActivityKcal
            )
        }
    }

    private fun elapsedMinutes(state: SessionModeUiState): Int {
        val seconds = java.time.Duration.between(
            state.sessionStartTime, LocalDateTime.now()
        ).seconds
        return (seconds / 60).coerceAtLeast(1).toInt()
    }

    /**
     * Prototype reward rule: ~0.7 particles per kcal, clamped 50..500.
     * Matches the "+225 Particles" shown in the design for a 320 kcal session.
     */
    private fun particlesEarned(activityKcal: Int): Int =
        (activityKcal * 0.7).toInt().coerceIn(50, 500)
}