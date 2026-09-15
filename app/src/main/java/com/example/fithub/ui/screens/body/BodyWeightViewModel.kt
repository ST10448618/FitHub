package com.example.fithub.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.calculator.WeightProgressCalculator
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.domain.model.WeightEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BodyWeightUiState(
    val isLoading: Boolean = true,

    // Weight data
    val currentWeightKg: Double? = null,
    val startingWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val goalDirection: GoalDirection = GoalDirection.MAINTAIN,

    // Progress
    val progressPercent: Int = 0,
    val deltaFromLastMeasureKg: Double? = null,
    val journey: List<WeightEntry> = emptyList(),

    // BMI
    val bmi: Double? = null,
    val bmiLabel: String = "—",

    // Checkpoint
    val nextCheckpointDate: LocalDate? = null,

    val errorMessage: String? = null
)

class BodyWeightViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(BodyWeightUiState())
    val uiState: StateFlow<BodyWeightUiState> = _uiState.asStateFlow()

    private val weightRepo = ServiceLocator.weightRepository
    private val userRepo = ServiceLocator.userRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val checkpointRepo = ServiceLocator.checkpointRepository

    init {
        observeAll()
    }

    private fun observeAll() {
        // All historical weigh-ins (for the journey chart)
        viewModelScope.launch {
            weightRepo.observeAll(uid).collect { list ->
                val sortedAsc = list.sortedBy { it.date }
                val latest = sortedAsc.lastOrNull()
                val previous = sortedAsc.dropLast(1).lastOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        journey = sortedAsc,
                        currentWeightKg = latest?.weightKg,
                        deltaFromLastMeasureKg = if (latest != null && previous != null)
                            latest.weightKg - previous.weightKg
                        else null,
                        bmi = latest?.let { entry ->
                            val heightCm = _uiState.value.bmi?.let { 0.0 } ?: 0.0
                            // recomputed below once we know height
                            null
                        }
                    )
                }

                recomputeDerived()
            }
        }

        // Profile (height + starting weight)
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update {
                    it.copy(startingWeightKg = profile?.startingWeightKg)
                }
                recomputeDerived(profileHeightCm = profile?.heightCm)
            }
        }

        // Goal
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

        // Checkpoint
        viewModelScope.launch {
            checkpointRepo.observe(uid).collect { schedule ->
                _uiState.update { it.copy(nextCheckpointDate = schedule?.nextCheckpointDate) }
            }
        }
    }

    private var profileHeight: Double? = null

    private fun recomputeDerived(profileHeightCm: Double? = null) {
        if (profileHeightCm != null) profileHeight = profileHeightCm

        val s = _uiState.value
        val current = s.currentWeightKg
        val starting = s.startingWeightKg ?: s.journey.firstOrNull()?.weightKg
        val target = s.targetWeightKg

        // Progress %
        val progress = if (current != null && starting != null && target != null) {
            WeightProgressCalculator.calculate(
                startingWeightKg = starting,
                currentWeightKg = current,
                targetWeightKg = target,
                direction = s.goalDirection
            ).clampedPercent
        } else 0

        // BMI
        val height = profileHeight
        val bmi = if (current != null && height != null && height > 0) {
            current / ((height / 100.0) * (height / 100.0))
        } else null

        val label = when {
            bmi == null -> "—"
            bmi < 18.5 -> "Under weight"
            bmi < 25.0 -> "Healthy"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }

        _uiState.update {
            it.copy(
                progressPercent = progress,
                bmi = bmi,
                bmiLabel = label
            )
        }
    }
}