package com.example.fithub.ui.screens.progress

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.core.i18n.LocaleManager
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import com.example.fithub.domain.model.SessionStatus
import com.example.fithub.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class GraphMode { WEEK, MONTH, YEAR }

/** One plotted point. */
data class GraphPoint(val label: String, val value: Float)

data class WorkoutsOverviewUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val graphMode: GraphMode = GraphMode.WEEK,

    val totalActivityKcal: Int = 0,
    val totalActiveMinutes: Int = 0,

    val monthlyGoalKcal: Int = 10000,
    val monthlyProgressPercent: Int = 0,

    val graphEntries: List<GraphPoint> = emptyList(),
    @param:StringRes val graphTitleRes: Int = R.string.workouts_overview_graph_title,

    val habits: WorkoutProgressCalculator.Habits = WorkoutProgressCalculator.Habits(0, 0, 0, 0),
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

        val monthSessions = allSessions.filter { it.logDate in from..to }
        val totalActivity = monthSessions.sumOf { it.estimatedActivityKcal }
        val totalMinutes = monthSessions.sumOf { it.durationMinutes }

        val goal = s.monthlyGoalKcal
        val progressPercent = if (goal > 0)
            ((totalActivity.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
        else 0

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
        val locale = Locale.forLanguageTag(
            LocaleManager.currentCode(ServiceLocator.appContext())
        )

        val points: List<GraphPoint> = when (mode) {
            GraphMode.WEEK -> {
                val dayFormatter = DateTimeFormatter.ofPattern("EEE", locale)
                val anchor = month.atDay(15)
                WorkoutProgressCalculator.weekGraph(allSessions, anchor).map { day ->
                    GraphPoint(
                        label = day.date.format(dayFormatter),
                        value = day.activityKcal.toFloat()
                    )
                }
            }
            GraphMode.MONTH -> {
                val short = DateTimeFormatter.ofPattern("d MMM", locale)
                WorkoutProgressCalculator.monthGraph(allSessions, month.atDay(1)).map { week ->
                    GraphPoint(
                        label = week.label,
                        value = week.activityKcal.toFloat()
                    )
                }
            }
            GraphMode.YEAR -> {
                val monthFormatter = DateTimeFormatter.ofPattern("MMM", locale)
                WorkoutProgressCalculator.yearGraph(allSessions, month.year).map { m ->
                    GraphPoint(
                        label = m.label,
                        value = m.activityKcal.toFloat()
                    )
                }
            }
        }

        _uiState.update { it.copy(graphEntries = points) }
    }
}