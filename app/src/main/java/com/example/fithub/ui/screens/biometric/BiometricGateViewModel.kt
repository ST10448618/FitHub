package com.example.fithub.ui.screens.biometric

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.LogTags
import com.example.fithub.core.PostLoginSync
import com.example.fithub.core.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BiometricGateUiState(
    val authenticated: Boolean = false,
    val isSyncing: Boolean = false,
    @param:StringRes val errorMessageResId: Int? = null
)

class BiometricGateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricGateUiState())
    val uiState: StateFlow<BiometricGateUiState> = _uiState.asStateFlow()

    fun onBiometricSuccess() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.w(LogTags.BIOMETRIC, "Biometric success but no Firebase user — cannot continue")
            _uiState.update { it.copy(errorMessageResId = null, authenticated = false) }
            return
        }
        if (_uiState.value.isSyncing) return
        _uiState.update { it.copy(isSyncing = true, errorMessageResId = null) }
        viewModelScope.launch {
            PostLoginSync.run(uid)
            Log.i(LogTags.BIOMETRIC, "Biometric gate unlock complete — navigating to dashboard")
            _uiState.update { it.copy(isSyncing = false, authenticated = true) }
        }
    }

    fun onUsePassword() {
        Log.i(LogTags.BIOMETRIC, "User chose password fallback — signing out")
        SessionManager.logout()
    }

    fun setError(@StringRes resId: Int) {
        _uiState.update { it.copy(errorMessageResId = resId) }
    }
}