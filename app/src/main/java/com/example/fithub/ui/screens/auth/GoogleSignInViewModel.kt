package com.example.fithub.ui.screens.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.OnboardingSession
import com.example.fithub.core.PostLoginSync
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.data.remote.auth.GoogleSignInHelper
import com.example.fithub.domain.repository.GoogleAuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Outcome the screen must act on. `null` = no action pending.
 */
sealed interface GoogleSignInOutcome {
    data object ExistingUser : GoogleSignInOutcome
    data object NeedsOnboarding : GoogleSignInOutcome
}

data class GoogleSignInUiState(
    val isWorking: Boolean = false,
    val error: String? = null,
    val outcome: GoogleSignInOutcome? = null
)

/**
 * Drives "Continue with Google" for both Login and Landing.
 *
 * Decision tree (Phase 2 spec §5):
 * 1. Resolve Google identity via Credential Manager.
 * 2. Exchange for Firebase session.
 * 3. Pull `users/{uid}` from Firestore.
 * 4. If `onboardingComplete == true` → [PostLoginSync.run] + `ExistingUser`.
 * 5. Else → seed [OnboardingSession] fields and set `isGoogleUser = true`,
 *    `googleUid = uid` → `NeedsOnboarding`.
 *
 * References:
 *  • https://developer.android.com/identity/sign-in/credential-manager-siwg
 *  • https://firebase.google.com/docs/auth/android/google-signin
 * (accessed February 2026)
 */
class GoogleSignInViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleSignInUiState())
    val uiState: StateFlow<GoogleSignInUiState> = _uiState.asStateFlow()

    private val authRepo = ServiceLocator.authRepository
    private val userRepo = ServiceLocator.userRepository

    fun start(context: Context) {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(isWorking = true, error = null, outcome = null) }
        viewModelScope.launch {
            try {
                val appCtx = context.applicationContext
                val serverClientId = runCatching {
                    appCtx.getString(R.string.default_web_client_id)
                }.getOrDefault("")
                if (serverClientId.isBlank()) {
                    // This only happens if google-services.json was not
                    // re-downloaded after enabling Google provider.
                    Log.e(LogTags.AUTH, "default_web_client_id missing — check google-services.json")
                }

                val helper = GoogleSignInHelper(appCtx)
                when (val acc = helper.signIn(serverClientId)) {
                    is Resource.Error -> {
                        Log.w(LogTags.AUTH, "Google sign-in aborted before Firebase exchange")
                        _uiState.update {
                            it.copy(isWorking = false, error = acc.message)
                        }
                    }
                    is Resource.Success -> exchangeWithFirebase(acc.data.idToken)
                    Resource.Loading -> Unit
                }
            } catch (t: Throwable) {
                Log.e(LogTags.AUTH, "Unexpected Google flow error", t)
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        error = context.getString(R.string.auth_google_error_generic)
                    )
                }
            }
        }
    }

    private suspend fun exchangeWithFirebase(idToken: String) {
        when (val firebase = authRepo.signInWithGoogle(idToken)) {
            is Resource.Error -> {
                _uiState.update { it.copy(isWorking = false, error = firebase.message) }
            }
            is Resource.Success -> classify(firebase.data)
            Resource.Loading -> Unit
        }
    }

    private suspend fun classify(result: GoogleAuthResult) {
        Log.i(
            LogTags.AUTH,
            "Google → Firebase uid=${result.uid.take(8)}… isNewUser=${result.isNewUser}"
        )
        userRepo.syncFromRemote(result.uid)
        val profile = userRepo.getProfile(result.uid)
        val isExisting = profile?.onboardingComplete == true
        if (isExisting) {
            Log.i(LogTags.AUTH, "Google user is existing — running PostLoginSync")
            PostLoginSync.run(result.uid)
            _uiState.update { it.copy(isWorking = false, outcome = GoogleSignInOutcome.ExistingUser) }
        } else {
            Log.i(LogTags.AUTH, "Google user is new — seeding OnboardingSession")
            OnboardingSession.reset()
            OnboardingSession.isGoogleUser = true
            OnboardingSession.googleUid = result.uid
            OnboardingSession.googleDisplayName = result.displayName
            OnboardingSession.username = result.displayName
            OnboardingSession.email = result.email
            _uiState.update { it.copy(isWorking = false, outcome = GoogleSignInOutcome.NeedsOnboarding) }
        }
    }

    /** Called by the screen after acting on [GoogleSignInOutcome], so it can be re-triggered. */
    fun consumeOutcome() {
        _uiState.update { it.copy(outcome = null) }
    }
}