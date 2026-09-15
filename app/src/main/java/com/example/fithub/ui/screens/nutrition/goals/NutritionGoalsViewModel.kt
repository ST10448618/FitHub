package com.example.fithub.ui.screens.nutrition.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.CalorieEngine
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.calculator.MacroCalculator
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class NutritionGoalsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Profile context
    val age: Int = 0,
    val currentWeightKg: Double = 0.0,
    val heightCm: Double = 0.0,
    val activityLabel: String = "",
    val targetWeightKg: Double? = null,

    // Recommendation engine output
    val recommendedDailyCalories: Int = 0,
    val goalDirection: GoalDirection = GoalDirection.MAINTAIN,

    // User-selected targets
    val userDailyCalories: Int = 2000,
    val breakfastKcal: Int = 0,
    val lunchKcal: Int = 0,
    val dinnerKcal: Int = 0,
    val snackKcal: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,

    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val mealAllocated: Int get() = breakfastKcal + lunchKcal + dinnerKcal + snackKcal
    val mealValid: Boolean get() = mealAllocated == userDailyCalories
}

class NutritionGoalsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(NutritionGoalsUiState())
    val uiState: StateFlow<NutritionGoalsUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val weightRepo = ServiceLocator.weightRepository
    private val nutritionRepo = ServiceLocator.nutritionGoalsRepository

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val profile = userRepo.getProfile(uid)
            val goal = goalRepo.getCurrent(uid)
            val latestWeight = weightRepo.getLatest(uid)

            if (profile == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Profile not found.")
                }
                return@launch
            }

            val currentWeight = latestWeight?.weightKg ?: profile.currentWeightKg
            val targetWeight = goal?.targetWeightKg
            val direction = if (targetWeight != null) {
                GoalDirectionCalculator.from(currentWeight, targetWeight)
            } else GoalDirection.MAINTAIN

            val recommended = CalorieEngine.calculate(
                CalorieEngine.Input(
                    age = profile.age,
                    gender = profile.gender,
                    heightCm = profile.heightCm,
                    weightKg = currentWeight,
                    activityLevel = profile.activityLevel,
                    goalDirection = direction
                )
            ).recommendedDailyCalories

            val existing = nutritionRepo.getCurrent(uid)

            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        age = profile.age,
                        currentWeightKg = currentWeight,
                        heightCm = profile.heightCm,
                        activityLabel = profile.activityLevel.label,
                        targetWeightKg = targetWeight,
                        recommendedDailyCalories = recommended,
                        goalDirection = direction,
                        userDailyCalories = existing.userDailyCalories,
                        breakfastKcal = existing.mealTargets.breakfastKcal,
                        lunchKcal = existing.mealTargets.lunchKcal,
                        dinnerKcal = existing.mealTargets.dinnerKcal,
                        snackKcal = existing.mealTargets.snackKcal,
                        proteinG = existing.macroTargets.proteinG,
                        carbsG = existing.macroTargets.carbsG,
                        fatG = existing.macroTargets.fatG
                    )
                }
            } else {
                // First time — seed with recommendation + starter allocation
                val meals = MacroCalculator.starterMealTargets(recommended)
                val macros = MacroCalculator.starterMacros(recommended)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        age = profile.age,
                        currentWeightKg = currentWeight,
                        heightCm = profile.heightCm,
                        activityLabel = profile.activityLevel.label,
                        targetWeightKg = targetWeight,
                        recommendedDailyCalories = recommended,
                        goalDirection = direction,
                        userDailyCalories = recommended,
                        breakfastKcal = meals.breakfastKcal,
                        lunchKcal = meals.lunchKcal,
                        dinnerKcal = meals.dinnerKcal,
                        snackKcal = meals.snackKcal,
                        proteinG = macros.proteinG,
                        carbsG = macros.carbsG,
                        fatG = macros.fatG
                    )
                }
            }
        }
    }

    fun acceptRecommendation() {
        val recommended = _uiState.value.recommendedDailyCalories
        val meals = MacroCalculator.starterMealTargets(recommended)
        _uiState.update {
            it.copy(
                userDailyCalories = recommended,
                breakfastKcal = meals.breakfastKcal,
                lunchKcal = meals.lunchKcal,
                dinnerKcal = meals.dinnerKcal,
                snackKcal = meals.snackKcal
            )
        }
        recalcMacrosFromCalories()
    }

    fun setUserDailyCalories(value: Int) {
        val safe = value.coerceIn(1000, 6000)
        _uiState.update { it.copy(userDailyCalories = safe) }
        // Rescale meals proportionally
        val state = _uiState.value
        val oldTotal = state.mealAllocated
        if (oldTotal > 0) {
            val scale = safe.toDouble() / oldTotal
            val newBreakfast = (state.breakfastKcal * scale).toInt()
            val newLunch = (state.lunchKcal * scale).toInt()
            val newDinner = (state.dinnerKcal * scale).toInt()
            val newSnack = safe - newBreakfast - newLunch - newDinner
            _uiState.update {
                it.copy(
                    breakfastKcal = newBreakfast,
                    lunchKcal = newLunch,
                    dinnerKcal = newDinner,
                    snackKcal = newSnack
                )
            }
        }
    }

    fun setMealCalories(type: MealType, value: Int) {
        val safe = value.coerceAtLeast(0)
        _uiState.update {
            when (type) {
                MealType.BREAKFAST -> it.copy(breakfastKcal = safe)
                MealType.LUNCH -> it.copy(lunchKcal = safe)
                MealType.DINNER -> it.copy(dinnerKcal = safe)
                MealType.SNACK -> it.copy(snackKcal = safe)
            }
        }
    }

    fun setProtein(value: Int) {
        _uiState.update { it.copy(proteinG = value.coerceIn(0, 500)) }
    }

    fun setCarbs(value: Int) {
        _uiState.update { it.copy(carbsG = value.coerceIn(0, 800)) }
    }

    fun setFat(value: Int) {
        _uiState.update { it.copy(fatG = value.coerceIn(0, 300)) }
    }

    fun recalcMacrosFromCalories() {
        val macros = MacroCalculator.starterMacros(_uiState.value.userDailyCalories)
        _uiState.update {
            it.copy(
                proteinG = macros.proteinG,
                carbsG = macros.carbsG,
                fatG = macros.fatG
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.mealValid) {
            _uiState.update {
                it.copy(errorMessage = "Meal targets must sum to your daily target.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val goals = NutritionGoals(
                id = IdGenerator.userSingletonId("nutrition_goals", uid),
                userId = uid,
                recommendedDailyCalories = state.recommendedDailyCalories,
                userDailyCalories = state.userDailyCalories,
                mealTargets = MealTargets(
                    breakfastKcal = state.breakfastKcal,
                    lunchKcal = state.lunchKcal,
                    dinnerKcal = state.dinnerKcal,
                    snackKcal = state.snackKcal
                ),
                macroTargets = MacroTargets(
                    proteinG = state.proteinG,
                    carbsG = state.carbsG,
                    fatG = state.fatG
                ),
                effectiveDate = LocalDate.now(),
                updatedAt = LocalDateTime.now()
            )

            when (val result = nutritionRepo.save(goals)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saved = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}