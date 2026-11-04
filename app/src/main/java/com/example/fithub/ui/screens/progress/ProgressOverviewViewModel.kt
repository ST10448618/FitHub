package com.example.fithub.ui.screens.progress

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.calculator.WeightProgressCalculator
import com.example.fithub.domain.model.GoalDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ProgressOverviewUiState(
    val isLoading: Boolean = true,

    val selectedMonth: YearMonth = YearMonth.now(),

    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val averageDailyIntakeKcal: Int = 0,
    val totalMonthlyActivityKcal: Int = 0,

    val startingWeightKg: Double? = null,
    val weightProgressPercent: Int = 0,
    val goalDirection: GoalDirection = GoalDirection.MAINTAIN,

    val bmi: Double? = null,
    @param:StringRes val bmiLabelRes: Int? = null,

    val dailyCalorieGoal: Int = 3000,
    val averageCaloriesByWeek: List<NutritionProgressCalculator.DashboardNutrition> = emptyList(),
    val weekBars: List<WeekBar> = emptyList(),

    val weeklyWorkoutGoal: Int = 4,
    val weeksInMonth: List<WeekWorkoutProgress> = emptyList(),

    val errorMessage: String? = null
)

/**
 * One bar in the per-week mini nutrition chart.
 * [labelRes] carries a `@StringRes` id (e.g. `progress_overview_week_short_format`)
 * with `%1$d` for the week number; the caller resolves it with `stringResource`.
 */
data class WeekBar(
    @param:StringRes val labelRes: Int,
    val weekNumber: Int,
    val kcal: Int,
    val goalKcal: Int
)

/**
 * One row in the per-week workout progress list.
 * [labelRes] carries a `@StringRes` id with `%1$d` for the week number.
 */
data class WeekWorkoutProgress(
    @param:StringRes val labelRes: Int,
    val weekNumber: Int,
    val completed: Int,
    val target: Int
)

class ProgressOverviewViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(ProgressOverviewUiState())
    val uiState: StateFlow<ProgressOverviewUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    private val weightRepo = ServiceLocator.weightRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val nutritionGoalsRepo = ServiceLocator.nutritionGoalsRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository

    init {
        observeAll()
    }

    fun onPreviousMonth() {
        _uiState.update {
            it.copy(selectedMonth = it.selectedMonth.minusMonths(1))
        }
        reload()
    }

    fun onNextMonth() {
        val next = _uiState.value.selectedMonth.plusMonths(1)
        if (next <= YearMonth.now()) {
            _uiState.update { it.copy(selectedMonth = next) }
            reload()
        }
    }

    private fun observeAll() {
        observeProfile()
        observeWeight()
        observeGoal()
        observeNutritionGoals()
        observeWorkoutGoals()
        reload()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update { it.copy(startingWeightKg = profile?.startingWeightKg) }
                recomputeDerived(profile?.heightCm)
            }
        }
    }

    private fun observeWeight() {
        viewModelScope.launch {
            weightRepo.observeLatest(uid).collect { w ->
                _uiState.update { it.copy(currentWeightKg = w?.weightKg, isLoading = false) }
                recomputeDerived()
            }
        }
    }

    private fun observeGoal() {
        viewModelScope.launch {
            goalRepo.observeCurrent(uid).collect { goal ->
                _uiState.update {
                    it.copy(
                        targetWeightKg = goal?.targetWeightKg,
                        goalDirection = goal?.direction ?: GoalDirection.MAINTAIN
                    )
                }
                recomputeDerived()
            }
        }
    }

    private fun observeNutritionGoals() {
        viewModelScope.launch {
            nutritionGoalsRepo.observeCurrent(uid).collect { goals ->
                _uiState.update {
                    it.copy(dailyCalorieGoal = goals?.userDailyCalories ?: 3000)
                }
                reloadNutrition()
            }
        }
    }

    private fun observeWorkoutGoals() {
        viewModelScope.launch {
            workoutGoalsRepo.observe(uid).collect { goals ->
                _uiState.update { it.copy(weeklyWorkoutGoal = goals?.sessionsPerWeek ?: 4) }
                reloadWorkouts()
            }
        }
    }

    private fun reload() {
        reloadNutrition()
        reloadWorkouts()
    }

    private fun reloadNutrition() {
        val month = _uiState.value.selectedMonth
        val from = month.atDay(1)
        val to = month.atEndOfMonth()

        viewModelScope.launch {
            val logs = foodLogRepo.getBetween(uid, from, to)
            val goals = nutritionGoalsRepo.getCurrent(uid)
            val dailyGoal = goals?.userDailyCalories ?: 3000

            val avg = NutritionProgressCalculator.averageDailyIntake(logs, from..to)
            val bars = buildWeekBars(logs, month, dailyGoal)

            _uiState.update {
                it.copy(
                    averageDailyIntakeKcal = avg.calories.toInt(),
                    weekBars = bars
                )
            }
        }
    }

    private fun buildWeekBars(
        logs: List<com.example.fithub.domain.model.FoodLog>,
        month: YearMonth,
        dailyGoal: Int
    ): List<WeekBar> {
        val firstOfMonth = month.atDay(1)
        val lastOfMonth = month.atEndOfMonth()
        val byDate = logs.groupBy { it.logDate }

        val bars = mutableListOf<WeekBar>()
        var cursor = firstOfMonth
        var weekIdx = 1
        while (cursor <= lastOfMonth) {
            val weekEnd = minOf(cursor.plusDays(6), lastOfMonth)
            val daysWithLogs = byDate.keys.filter { it in cursor..weekEnd }
            val weekAvg = if (daysWithLogs.isEmpty()) {
                0
            } else {
                val total = daysWithLogs.sumOf { day ->
                    byDate[day]?.sumOf { it.calories } ?: 0.0
                }
                (total / daysWithLogs.size).toInt()
            }
            bars.add(
                WeekBar(
                    labelRes = R.string.progress_overview_week_short_format,
                    weekNumber = weekIdx,
                    kcal = weekAvg,
                    goalKcal = dailyGoal
                )
            )
            cursor = weekEnd.plusDays(1)
            weekIdx++
        }
        return bars
    }

    private fun reloadWorkouts() {
        val month = _uiState.value.selectedMonth
        val from = month.atDay(1)
        val to = month.atEndOfMonth()

        viewModelScope.launch {
            val sessions = sessionRepo.getBetween(uid, from, to)
            val target = _uiState.value.weeklyWorkoutGoal

            val totalActivity = sessions.sumOf { it.estimatedActivityKcal }
            val weeks = buildWeekWorkoutProgress(sessions, month, target)

            _uiState.update {
                it.copy(
                    totalMonthlyActivityKcal = totalActivity,
                    weeksInMonth = weeks
                )
            }
        }
    }

    private fun buildWeekWorkoutProgress(
        sessions: List<com.example.fithub.domain.model.WorkoutSession>,
        month: YearMonth,
        weeklyTarget: Int
    ): List<WeekWorkoutProgress> {
        val firstOfMonth = month.atDay(1)
        val lastOfMonth = month.atEndOfMonth()

        val result = mutableListOf<WeekWorkoutProgress>()
        var cursor = firstOfMonth
        var idx = 1
        while (cursor <= lastOfMonth) {
            val weekEnd = minOf(cursor.plusDays(6), lastOfMonth)
            val count = sessions.count { it.logDate in cursor..weekEnd }
            result.add(
                WeekWorkoutProgress(
                    labelRes = R.string.progress_overview_week_label_format,
                    weekNumber = idx,
                    completed = count,
                    target = weeklyTarget
                )
            )
            cursor = weekEnd.plusDays(1)
            idx++
        }
        return result
    }

    private var profileHeight: Double? = null

    private fun recomputeDerived(profileHeightCm: Double? = null) {
        if (profileHeightCm != null) profileHeight = profileHeightCm

        val s = _uiState.value
        val current = s.currentWeightKg
        val starting = s.startingWeightKg
        val target = s.targetWeightKg

        val progress = if (current != null && starting != null && target != null) {
            WeightProgressCalculator.calculate(
                startingWeightKg = starting,
                currentWeightKg = current,
                targetWeightKg = target,
                direction = s.goalDirection
            ).clampedPercent
        } else 0

        val height = profileHeight
        val bmi = if (current != null && height != null && height > 0) {
            current / ((height / 100.0) * (height / 100.0))
        } else null

        val labelRes: Int? = when {
            bmi == null -> null
            bmi < 18.5 -> R.string.bmi_underweight
            bmi < 25.0 -> R.string.bmi_healthy
            bmi < 30.0 -> R.string.bmi_overweight
            else -> R.string.bmi_obese
        }

        _uiState.update {
            it.copy(
                weightProgressPercent = progress,
                bmi = bmi,
                bmiLabelRes = labelRes
            )
        }
    }
}