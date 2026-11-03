package com.example.fithub.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.OnboardingSession
import com.example.fithub.core.PostLoginSync
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class OnboardingSubmitState(
    val isSubmitting: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel : ViewModel() {

    private val _submitState = MutableStateFlow(OnboardingSubmitState())
    val submitState: StateFlow<OnboardingSubmitState> = _submitState.asStateFlow()

    fun submit() {
        if (!OnboardingSession.isReadyToSubmit()) {
            val ctx = ServiceLocator.appContext()
            _submitState.update {
                it.copy(errorMessage = ctx.getString(R.string.onboarding_missing_details))
            }
            return
        }

        viewModelScope.launch {
            _submitState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val ctx = ServiceLocator.appContext()

            val uid: String = if (OnboardingSession.isGoogleUser) {
                val existing = OnboardingSession.googleUid
                    ?: ServiceLocator.authRepository.currentUserId()
                if (existing.isNullOrBlank()) {
                    Log.e(LogTags.AUTH, "Google onboarding has no uid — aborting")
                    _submitState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = ctx.getString(R.string.auth_google_error_generic)
                        )
                    }
                    return@launch
                }
                Log.i(LogTags.AUTH, "Google onboarding reusing existing uid=${existing.take(8)}…")
                existing
            } else {
                val registerResult = ServiceLocator.authRepository.register(
                    OnboardingSession.email,
                    OnboardingSession.password
                )
                if (registerResult is Resource.Error) {
                    _submitState.update {
                        it.copy(isSubmitting = false, errorMessage = registerResult.message)
                    }
                    return@launch
                }
                (registerResult as Resource.Success).data
            }

            val now = LocalDateTime.now()

            val profile = UserProfile(
                id = uid,
                firstName = OnboardingSession.username,
                surname = "",
                email = OnboardingSession.email,
                gender = OnboardingSession.gender ?: Gender.OTHER,
                age = OnboardingSession.age ?: 0,
                heightCm = OnboardingSession.heightCm ?: 0.0,
                startingWeightKg = OnboardingSession.weightKg ?: 0.0,
                currentWeightKg = OnboardingSession.weightKg ?: 0.0,
                activityLevel = OnboardingSession.activityLevel ?: ActivityLevel.SEDENTARY,
                biometricEnabled = OnboardingSession.biometricEnabled,
                onboardingComplete = true,
                createdAt = now,
                updatedAt = now
            )

            val createResult = ServiceLocator.userRepository.createProfile(profile)
            if (createResult is Resource.Error) {
                _submitState.update {
                    it.copy(isSubmitting = false, errorMessage = createResult.message)
                }
                return@launch
            }

            ServiceLocator.weightRepository.addEntry(
                WeightEntry(
                    id = IdGenerator.newId(),
                    userId = uid,
                    weightKg = profile.startingWeightKg,
                    date = now.toLocalDate(),
                    createdAt = now
                )
            )
            ServiceLocator.workoutGoalsRepository.save(
                WorkoutGoals(
                    id = IdGenerator.newId(),
                    userId = uid,
                    sessionsPerWeek = 4,
                    monthlyActivityGoalKcal = 10000
                )
            )
            ServiceLocator.checkpointRepository.save(
                CheckpointSchedule(
                    id = IdGenerator.newId(),
                    userId = uid,
                    frequencyDays = 14,
                    nextCheckpointDate = now.toLocalDate().plusDays(14),
                    remindersEnabled = true
                )
            )

            PostLoginSync.run(uid)

            OnboardingSession.reset()
            Log.i(LogTags.AUTH, "Onboarding submit complete uid=${uid.take(8)}…")
            _submitState.update { it.copy(isSubmitting = false, isComplete = true) }
        }
    }
}