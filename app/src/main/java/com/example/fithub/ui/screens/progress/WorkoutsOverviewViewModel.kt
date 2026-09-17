package com.example.fithub.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import com.example.fithub.domain.model.SessionStatus
import com.example.fithub.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class GraphMode { WEEK, MONTH, YEAR }

data class WorkoutsOverviewUiState(
    val isLoading: Boolean = true,

    // Period
    val selectedMonth: YearMonth = YearMonth.now(),
    val graphMode: GraphMode = GraphMode.WEEK,

    // Top tiles
    val totalActivityKcal: Int = 0,
    val totalActiveMinutes: Int = 0,

    // Monthly goal
    val monthlyGoalKcal: Int = 10000,
    val monthlyProgressPercent: Int = 0,

    // Graph
    val graphEntries: List<Pair<String, Float>> = emptyList(),
    val graphTitle: String = "Estimated Calories Burnt",

    // Habits
    val habits: WorkoutProgressCalculator.Habits = WorkoutProgressCalculator.Habits(0, 0, 0, 0),

    // Category breakdown
    val categoryBreakdown: List<WorkoutProgressCalculator.CategoryCount> = emptyList(),
    val totalSessions: Int = 0,

    val errorMessage: String? = null
)

class WorkoutsOverviewViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(WorkoutsOverviewUiState())
    val uiState: StateFlow<WorkoutsOverviewUiState> = _uiState.asStateFlow()

    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository

    // Sessions cache used for all computed views
    private var allSessions: List<WorkoutSession> = emptyList()

    init {
        observeGoals()
        loadSessions()
    }

    private fun observeGoals() {
        viewModelScope.launch {
            workoutGoalsRepo.observe(uid).collect { goals ->
                val target = goals?.monthlyActivityGoalKcal ?: 10000
                _uiState.update { it.copy(monthlyGoalKcal = target) }
                recompute()
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            sessionRepo.observeCompleted(uid).collect { sessions ->
                allSessions = sessions.filter { it.status == SessionStatus.COMPLETED }
                _uiState.update { it.copy(isLoading = false) }
                recompute()
            }
        }
    }

    fun onPreviousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
        recompute()
    }

    fun onNextMonth() {
        val next = _uiState.value.selectedMonth.plusMonths(1)
        if (next <= YearMonth.now()) {
            _uiState.update { it.copy(selectedMonth = next) }
            recompute()
        }
    }

    fun onGraphModeChange(mode: GraphMode) {
        _uiState.update { it.copy(graphMode = mode) }
        rebuildGraph()
    }

    private fun recompute() {
        val s = _uiState.value
        val month = s.selectedMonth
        val from = month.atDay(1)
        val to = month.atEndOfMonth()

        // Top tiles — scoped to the selected month
        val monthSessions = allSessions.filter { it.logDate in from..to }
        val totalActivity = monthSessions.sumOf { it.estimatedActivityKcal }
        val totalMinutes = monthSessions.sumOf { it.durationMinutes }

        val goal = s.monthlyGoalKcal
        val progressPercent = if (goal > 0)
            ((totalActivity.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
        else 0

        // Habits and breakdown — all-time, since the design shows aggregate stats
        val habits = WorkoutProgressCalculator.habits(allSessions)
        val breakdown = WorkoutProgressCalculator.categoryBreakdown(allSessions)

        _uiState.update {
            it.copy(
                totalActivityKcal = totalActivity,
                totalActiveMinutes = totalMinutes,
                monthlyProgressPercent = progressPercent,
                habits = habits,
                categoryBreakdown = breakdown,
                totalSessions = allSessions.size
            )
        }

        rebuildGraph()
    }

    private fun rebuildGraph() {
        val s = _uiState.value
        val month = s.selectedMonth
        val mode = s.graphMode

        val pairs: List<Pair<String, Float>> = when (mode) {
            GraphMode.WEEK -> {
                // Days Mon..Sun of the week containing the 15th of the selected month
                val anchor = month.atDay(15)
                WorkoutProgressCalculator.weekGraph(allSessions, anchor).map { day ->
                    day.date.dayOfWeek.name.take(3)
                        .lowercase()
                        .replaceFirstChar { it.uppercase() } to day.activityKcal.toFloat()
                }
            }
            GraphMode.MONTH -> {
                WorkoutProgressCalculator.monthGraph(allSessions, month.atDay(1)).map { week ->
                    week.label to week.activityKcal.toFloat()
                }
            }
            GraphMode.YEAR -> {
                WorkoutProgressCalculator.yearGraph(allSessions, month.year).map { m ->
                    m.label to m.activityKcal.toFloat()
                }
            }
        }

        _uiState.update { it.copy(graphEntries = pairs) }
    }
}