package com.example.fithub.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.catalog.RewardCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.fithub.domain.catalog.RewardOffer


data class RewardsUiState(
    val isLoading: Boolean = true,
    val particleBalance: Int = 0,
    val offers: List<RewardOffer> = RewardCatalog.OFFERS,
    val statuses: Map<String, RewardCatalog.RewardStatus> = emptyMap(),
    val toastMessage: String? = null
)

class RewardsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val rewardRepo = ServiceLocator.rewardRepository
    private val prefs = ServiceLocator.preferencesManager

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            combine(
                rewardRepo.observeBalance(uid),
                prefs.claimedRewards
            ) { balance, claimed ->
                (balance?.balance ?: 0) to claimed
            }.collect { (balance, claimed) ->
                val statuses = RewardCatalog.OFFERS.associate { offer ->
                    offer.id to RewardCatalog.statusFor(
                        offer = offer,
                        currentBalance = balance,
                        claimedIds = claimed
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        particleBalance = balance,
                        statuses = statuses
                    )
                }
            }
        }
    }

    fun claimReward(offerId: String) {
        val offer = RewardCatalog.OFFERS.firstOrNull { it.id == offerId } ?: return
        viewModelScope.launch {
            val status = _uiState.value.statuses[offerId]
            when (status) {
                RewardCatalog.RewardStatus.CLAIMED -> {
                    _uiState.update { it.copy(toastMessage = "Already claimed.") }
                    return@launch
                }
                RewardCatalog.RewardStatus.INSUFFICIENT -> {
                    _uiState.update {
                        it.copy(toastMessage = "Need ${offer.costParticles} ✨ to claim.")
                    }
                    return@launch
                }
                RewardCatalog.RewardStatus.AVAILABLE, null -> Unit
            }

            rewardRepo.addParticles(uid, -offer.costParticles)
            prefs.addClaimedReward(offerId)
            _uiState.update {
                it.copy(toastMessage = "${offer.title} claimed!")
            }
        }
    }

    fun consumeToast() = _uiState.update { it.copy(toastMessage = null) }
}