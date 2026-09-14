package com.example.fithub.ui.screens.food.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.Food
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMealUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Food> = emptyList(),
    val errorMessage: String? = null,
    val recent: List<Food> = emptyList()
)

class AddMealViewModel : ViewModel() {

    private val foodRepo = ServiceLocator.foodRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q, errorMessage = null) }
        searchJob?.cancel()

        val trimmed = q.trim()
        if (trimmed.length < 3) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(700) // longer debounce — OFF rate-limits aggressively
            _uiState.update { it.copy(isSearching = true) }
            when (val result = foodRepo.search(trimmed)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSearching = false, results = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSearching = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun loadCategory(category: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, results = emptyList()) }
            when (val result = foodRepo.getByCategory(category)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSearching = false, results = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSearching = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}