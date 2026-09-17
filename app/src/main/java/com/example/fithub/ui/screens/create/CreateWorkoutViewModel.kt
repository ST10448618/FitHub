package com.example.fithub.ui.screens.plans.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.calculator.WorkoutEstimator
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class CreateWorkoutUiState(
    val draft: CreateWorkoutSession.Draft = CreateWorkoutSession.Draft(),
    val exerciseCatalog: List<Exercise> = emptyList(),
    val selectedCategory: ExerciseCategory = ExerciseCategory.CHEST,
    val exerciseSearchQuery: String = "",
    val filteredExercises: List<Exercise> = emptyList(),
    val estimatedActivityKcal: Int = 0,
    val estimatedDurationMinutes: Int = 0,
    val isCreating: Boolean = false,
    val created: Boolean = false,
    val errorMessage: String? = null
)

class CreateWorkoutViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(CreateWorkoutUiState())
    val uiState: StateFlow<CreateWorkoutUiState> = _uiState.asStateFlow()

    private val exerciseRepo = ServiceLocator.exerciseRepository
    private val planRepo = ServiceLocator.workoutPlanRepository

    init {
        _uiState.update { it.copy(draft = CreateWorkoutSession.draft) }
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepo.observeAll().collect { all ->
                _uiState.update { it.copy(exerciseCatalog = all) }
                refreshFiltered()
                recalculate()
            }
        }
    }

    // ---------- STEP 1 ----------

    fun updateName(name: String) {
        CreateWorkoutSession.update { it.copy(name = name) }
        syncDraft()
    }

    fun updateDescription(desc: String) {
        CreateWorkoutSession.update { it.copy(description = desc) }
        syncDraft()
    }

    fun updateDifficulty(difficulty: WorkoutDifficulty) {
        CreateWorkoutSession.update { it.copy(difficulty = difficulty) }
        syncDraft()
    }

    fun updateEquipment(equipment: String) {
        CreateWorkoutSession.update { it.copy(equipment = equipment) }
        syncDraft()
    }

    // ---------- STEP 2 ----------

    fun selectCategory(category: ExerciseCategory) {
        _uiState.update { it.copy(selectedCategory = category, exerciseSearchQuery = "") }
        refreshFiltered()
    }

    fun onExerciseSearchChange(q: String) {
        _uiState.update { it.copy(exerciseSearchQuery = q) }
        refreshFiltered()
    }

    private fun refreshFiltered() {
        val s = _uiState.value
        val list = if (s.exerciseSearchQuery.isBlank()) {
            s.exerciseCatalog.filter { it.category == s.selectedCategory }
        } else {
            s.exerciseCatalog.filter {
                it.name.contains(s.exerciseSearchQuery, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(filteredExercises = list) }
    }

    fun isExerciseAdded(exerciseId: String): Boolean =
        _uiState.value.draft.exercises.any { it.exerciseId == exerciseId }

    fun addExercise(exercise: Exercise) {
        if (isExerciseAdded(exercise.id)) return

        val current = _uiState.value.draft.exercises
        val newSlot = WorkoutExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            category = exercise.category,
            orderIndex = current.size,
            targetReps = if (exercise.isTimeBased) 0 else 10,
            targetDurationSeconds = if (exercise.isTimeBased) 60 else 0,
            estimatedActivityKcal = WorkoutEstimator.exerciseActivity(
                exercise,
                targetReps = 10,
                targetDurationSeconds = 60
            )
        )

        CreateWorkoutSession.update {
            it.copy(exercises = current + newSlot)
        }
        syncDraft()
        recalculate()
    }

    fun removeExercise(exerciseId: String) {
        val updated = _uiState.value.draft.exercises
            .filterNot { it.exerciseId == exerciseId }
        CreateWorkoutSession.update { it.copy(exercises = updated) }
        CreateWorkoutSession.normaliseOrder()
        syncDraft()
        recalculate()
    }

    // ---------- STEP 3 ----------

    fun setReps(exerciseId: String, reps: Int) {
        val catalog = _uiState.value.exerciseCatalog.toMapById()
        val updated = _uiState.value.draft.exercises.map { ex ->
            if (ex.exerciseId == exerciseId) {
                val catalogEx = catalog[ex.exerciseId]
                val activity = if (catalogEx != null) {
                    WorkoutEstimator.exerciseActivity(
                        catalogEx,
                        targetReps = reps,
                        targetDurationSeconds = ex.targetDurationSeconds
                    )
                } else ex.estimatedActivityKcal
                ex.copy(targetReps = reps.coerceAtLeast(1), estimatedActivityKcal = activity)
            } else ex
        }
        CreateWorkoutSession.update { it.copy(exercises = updated) }
        syncDraft()
        recalculate()
    }

    fun setDuration(exerciseId: String, seconds: Int) {
        val catalog = _uiState.value.exerciseCatalog.toMapById()
        val updated = _uiState.value.draft.exercises.map { ex ->
            if (ex.exerciseId == exerciseId) {
                val catalogEx = catalog[ex.exerciseId]
                val activity = if (catalogEx != null) {
                    WorkoutEstimator.exerciseActivity(
                        catalogEx,
                        targetReps = ex.targetReps,
                        targetDurationSeconds = seconds
                    )
                } else ex.estimatedActivityKcal
                ex.copy(
                    targetDurationSeconds = seconds.coerceAtLeast(15),
                    estimatedActivityKcal = activity
                )
            } else ex
        }
        CreateWorkoutSession.update { it.copy(exercises = updated) }
        syncDraft()
        recalculate()
    }

    fun moveUp(exerciseId: String) {
        val list = _uiState.value.draft.exercises.toMutableList()
        val idx = list.indexOfFirst { it.exerciseId == exerciseId }
        if (idx <= 0) return
        val tmp = list[idx]
        list[idx] = list[idx - 1]
        list[idx - 1] = tmp
        CreateWorkoutSession.update { it.copy(exercises = list) }
        CreateWorkoutSession.normaliseOrder()
        syncDraft()
    }

    fun moveDown(exerciseId: String) {
        val list = _uiState.value.draft.exercises.toMutableList()
        val idx = list.indexOfFirst { it.exerciseId == exerciseId }
        if (idx < 0 || idx >= list.lastIndex) return
        val tmp = list[idx]
        list[idx] = list[idx + 1]
        list[idx + 1] = tmp
        CreateWorkoutSession.update { it.copy(exercises = list) }
        CreateWorkoutSession.normaliseOrder()
        syncDraft()
    }

    // ---------- STEP 4 ----------

    fun createPlan() {
        val draft = _uiState.value.draft
        if (draft.name.isBlank() || draft.exercises.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please complete all steps.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }

            val plan = WorkoutPlan(
                id = IdGenerator.newId(),
                userId = uid,
                name = draft.name.trim(),
                description = draft.description.trim(),
                category = WorkoutCategory.CREATED,
                difficulty = draft.difficulty,
                equipment = draft.equipment,
                imageUrl = draft.imageUrl,
                exercises = draft.exercises,
                estimatedActivityKcal = _uiState.value.estimatedActivityKcal,
                estimatedDurationMinutes = _uiState.value.estimatedDurationMinutes,
                isVerified = false,
                isSaved = false,
                createdAt = LocalDateTime.now()
            )

            when (val result = planRepo.saveCreated(uid, plan)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isCreating = false, created = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isCreating = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun cancelAndReset() {
        CreateWorkoutSession.reset()
        _uiState.update {
            it.copy(draft = CreateWorkoutSession.draft, estimatedActivityKcal = 0, estimatedDurationMinutes = 0)
        }
    }

    // ---------- HELPERS ----------

    private fun syncDraft() {
        _uiState.update { it.copy(draft = CreateWorkoutSession.draft) }
    }

    private fun recalculate() {
        val s = _uiState.value
        val catalog = s.exerciseCatalog.toMapById()
        val normalised = WorkoutEstimator.recalculate(s.draft.exercises, catalog)
        CreateWorkoutSession.update { it.copy(exercises = normalised) }

        val totalActivity = WorkoutEstimator.totalActivity(normalised)
        val totalDuration = WorkoutEstimator.totalDurationMinutes(normalised, catalog)

        _uiState.update {
            it.copy(
                draft = CreateWorkoutSession.draft,
                estimatedActivityKcal = totalActivity,
                estimatedDurationMinutes = totalDuration
            )
        }
    }

    private fun List<Exercise>.toMapById(): Map<String, Exercise> =
        associateBy { it.id }
}