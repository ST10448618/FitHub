package com.example.fithub.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.OnboardingSession
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

    /**
     * Called from the "Confirm" on the Onboarding Complete overlay.
     * Creates the Firebase user, writes the profile to Room, and creates
     * default goals + checkpoint schedule.
     */
    fun submit() {
        if (!OnboardingSession.isReadyToSubmit()) {
            _submitState.update { it.copy(errorMessage = "Some onboarding details are missing.") }
            return
        }

        viewModelScope.launch {
            _submitState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val auth = ServiceLocator.authRepository
            val registerResult = auth.register(OnboardingSession.email, OnboardingSession.password)
            if (registerResult is Resource.Error) {
                _submitState.update {
                    it.copy(isSubmitting = false, errorMessage = registerResult.message)
                }
                return@launch
            }

            val uid = (registerResult as Resource.Success).data
            val now = LocalDateTime.now()

            val profile = UserProfile(
                id = uid,
                firstName = OnboardingSession.username,      // single field per screenshot
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

            com.example.fithub.core.SessionManager.setUser(uid)

            // Initial weight entry — starting weight
            ServiceLocator.weightRepository.addEntry(
                WeightEntry(
                    id = IdGenerator.newId(),
                    userId = uid,
                    weightKg = profile.startingWeightKg,
                    date = now.toLocalDate(),
                    createdAt = now
                )
            )

            // Default workout goals
            ServiceLocator.workoutGoalsRepository.save(
                WorkoutGoals(
                    id = IdGenerator.newId(),
                    userId = uid,
                    sessionsPerWeek = 4,
                    monthlyActivityGoalKcal = 10000
                )
            )

            // Default checkpoint schedule
            ServiceLocator.checkpointRepository.save(
                CheckpointSchedule(
                    id = IdGenerator.newId(),
                    userId = uid,
                    frequencyDays = 14,
                    nextCheckpointDate = now.toLocalDate().plusDays(14),
                    remindersEnabled = true
                )
            )

            // NOTE: NutritionGoals and Goal are set by the user later via Goals section
            // (per master spec: target weight is NOT set during onboarding).

            OnboardingSession.reset()
            _submitState.update { it.copy(isSubmitting = false, isComplete = true) }
        }
    }
}