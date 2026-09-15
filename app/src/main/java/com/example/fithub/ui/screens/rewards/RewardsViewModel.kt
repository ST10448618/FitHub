package com.example.fithub.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.catalog.RewardCatalog
import com.example.fithub.domain.catalog.RewardOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RewardsUiState(
    val isLoading: Boolean = true,
    val particleBalance: Int = 0,
    val offers: List<RewardOffer> = RewardCatalog.OFFERS,
    val statuses: Map<String, RewardCatalog.RewardStatus> = emptyMap()
)

class RewardsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    private val rewardRepo = ServiceLocator.rewardRepository

    init {
        observeBalance()
    }

    private fun observeBalance() {
        viewModelScope.launch {
            rewardRepo.observeBalance(uid).collect { balance ->
                val current = balance?.balance ?: 0
                val statuses: Map<String, RewardCatalog.RewardStatus> =
                    _uiState.value.offers.associate { offer ->
                        offer.id to RewardCatalog.statusFor(
                            offer = offer,
                            currentBalance = current,
                            claimedIds = emptySet()
                        )
                    }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        particleBalance = current,
                        statuses = statuses
                    )
                }
            }
        }
    }
}