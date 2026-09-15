package com.example.fithub.ui.screens.goals.checkpoint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.CheckpointSchedule
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class CheckpointManagerUiState(
    val isLoading: Boolean = true,
    val frequencyDays: Int = 14,
    val nextCheckpointDate: LocalDate = LocalDate.now().plusDays(14),
    val remindersEnabled: Boolean = true,
    val lastRecordedWeightKg: Int? = null,
    val upcomingDates: List<LocalDate> = emptyList(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val errorMessage: String? = null
)

class CheckpointManagerViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(CheckpointManagerUiState())
    val uiState: StateFlow<CheckpointManagerUiState> = _uiState.asStateFlow()

    private val repo = ServiceLocator.checkpointRepository
    private val weightRepo = ServiceLocator.weightRepository

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val schedule = repo.get(uid)
            val latest = weightRepo.getLatest(uid)

            val frequency = schedule?.frequencyDays ?: 14
            val next = schedule?.nextCheckpointDate ?: LocalDate.now().plusDays(frequency.toLong())
            val reminders = schedule?.remindersEnabled ?: true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    frequencyDays = frequency,
                    nextCheckpointDate = next,
                    remindersEnabled = reminders,
                    lastRecordedWeightKg = latest?.weightKg?.toInt(),
                    upcomingDates = computeUpcoming(next, frequency)
                )
            }
        }
    }

    private fun computeUpcoming(next: LocalDate, frequency: Int): List<LocalDate> =
        (0..3).map { next.plusDays((frequency * it).toLong()) }

    fun onFrequencyChange(days: Int) {
        val next = LocalDate.now().plusDays(days.toLong())
        _uiState.update {
            it.copy(
                frequencyDays = days,
                nextCheckpointDate = next,
                upcomingDates = computeUpcoming(next, days)
            )
        }
    }

    fun onRemindersToggle(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val schedule = CheckpointSchedule(
                id = IdGenerator.newId(),
                userId = uid,
                frequencyDays = state.frequencyDays,
                nextCheckpointDate = state.nextCheckpointDate,
                remindersEnabled = state.remindersEnabled,
                updatedAt = LocalDateTime.now()
            )
            when (val result = repo.save(schedule)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saveComplete = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}