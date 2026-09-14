package com.example.fithub.ui.screens.food.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.FoodLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FoodLogListUiState(
    val isLoading: Boolean = true,
    val dateLabel: String = "",
    val logs: List<FoodLog> = emptyList()
)

class FoodLogListViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val dateStr: String = savedStateHandle["date"] ?: LocalDate.now().toString()
    private val date: LocalDate = runCatching { LocalDate.parse(dateStr) }
        .getOrDefault(LocalDate.now())

    private val foodLogRepo = ServiceLocator.foodLogRepository

    private val _uiState = MutableStateFlow(
        FoodLogListUiState(
            dateLabel = date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        )
    )
    val uiState: StateFlow<FoodLogListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            foodLogRepo.observeByDate(uid, date).collect { logs ->
                _uiState.update { it.copy(isLoading = false, logs = logs) }
            }
        }
    }
}