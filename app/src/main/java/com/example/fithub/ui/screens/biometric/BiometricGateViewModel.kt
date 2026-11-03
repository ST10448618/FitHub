package com.example.fithub.ui.screens.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.LogTags
import com.example.fithub.core.PostLoginSync
import com.example.fithub.core.SessionManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for [BiometricGateScreen].
 *
 * [authenticated] flips to `true` only after the post-biometric hydration
 * ([PostLoginSync.run]) finishes, so the nav callback that navigates to the
 * dashboard is guaranteed to arrive after Room is populated.
 */
data class BiometricGateUiState(
    val authenticated: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessageResId: Int? = null
)

class BiometricGateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricGateUiState())
    val uiState: StateFlow<BiometricGateUiState> = _uiState.asStateFlow()

    /**
     * Called by [BiometricGateScreen] after `BiometricPrompt` reports success.
     * Runs [PostLoginSync] for the currently signed-in UID then flips state.
     */
    fun onBiometricSuccess() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.w(LogTags.BIOMETRIC, "Biometric success but no Firebase user — cannot continue")
            _uiState.update { it.copy(errorMessageResId = null, authenticated = false) }
            // Screen will call onUsePassword to reset — no exception here.
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

    /** User chose "Use password instead" — sign out so the login screen starts clean. */
    fun onUsePassword() {
        Log.i(LogTags.BIOMETRIC, "User chose password fallback — signing out")
        SessionManager.logout()
    }

    /** Sets a resource-id error to display (never a raw English string). */
    fun setError(resId: Int) {
        _uiState.update { it.copy(errorMessageResId = resId) }
    }
}