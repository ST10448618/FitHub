package com.example.fithub.ui.screens.body

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.util.IdGenerator
import com.example.fithub.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime

data class CheckpointLoggerUiState(
    val isLoading: Boolean = true,
    val weight: Int = 70,

    val previousWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val goalDirection: GoalDirection = GoalDirection.MAINTAIN,

    /** The photo the user just picked (not yet saved locally). */
    val pendingPhotoUri: Uri? = null,
    /**
     * Phase 6B (revised): photos are now stored locally — the "progress"
     * field is kept for API compatibility with the screen but is never
     * incremented during a local file copy. It exists only if we ever
     * reintroduce remote sync in a future phase.
     */
    val photoUploadPercent: Int = 0,

    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

class CheckpointLoggerViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""
    private val weightRepo = ServiceLocator.weightRepository
    private val goalRepo = ServiceLocator.goalRepository

    private val _uiState = MutableStateFlow(CheckpointLoggerUiState())
    val uiState: StateFlow<CheckpointLoggerUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val latest = weightRepo.getLatest(uid)
            val goal = goalRepo.getCurrent(uid)
            val initial = latest?.weightKg?.toInt() ?: 70
            _uiState.update {
                it.copy(
                    isLoading = false,
                    weight = initial,
                    previousWeightKg = latest?.weightKg,
                    targetWeightKg = goal?.targetWeightKg,
                    goalDirection = goal?.direction ?: GoalDirection.MAINTAIN
                )
            }
        }
    }

    fun onWeightChange(newWeight: Int) {
        _uiState.update { it.copy(weight = newWeight.coerceIn(20, 400)) }
    }

    fun attachPhoto(uri: Uri) {
        Log.i(LogTags.STORAGE, "attachPhoto uri=$uri")
        _uiState.update { it.copy(pendingPhotoUri = uri, errorMessage = null) }
    }

    fun clearPhoto() {
        _uiState.update { it.copy(pendingPhotoUri = null, photoUploadPercent = 0) }
    }

    fun save() {
        val state = _uiState.value
        val now = LocalDateTime.now()
        val entryId = IdGenerator.newId()
        val pendingUri = state.pendingPhotoUri

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // 1. If a photo was picked, compress + copy to the app's private
            //    storage. No network. No Firebase Storage billing required.
            var localPhotoPath: String? = null
            if (pendingUri != null) {
                val bytes = withContext(Dispatchers.IO) {
                    ImageCompressor.compress(ServiceLocator.appContext(), pendingUri)
                }
                if (bytes == null) {
                    Log.w(LogTags.STORAGE, "compress returned null — saving entry without photo")
                } else {
                    localPhotoPath = withContext(Dispatchers.IO) {
                        savePhotoLocally(entryId, bytes)
                    }
                    if (localPhotoPath == null) {
                        Log.w(LogTags.STORAGE, "local photo save failed — saving entry without photo")
                    } else {
                        Log.i(LogTags.STORAGE, "photo saved locally: $localPhotoPath")
                    }
                }
            }

            // 2. Write the entry, with the local photo path if we got one.
            val entry = WeightEntry(
                id = entryId,
                userId = uid,
                weightKg = state.weight.toDouble(),
                date = LocalDate.now(),
                createdAt = now,
                photoUrl = localPhotoPath
            )

            when (val result = weightRepo.addEntry(entry)) {
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

    /**
     * Copies compressed JPEG bytes into `filesDir/progress_photos/{entryId}.jpg`
     * and returns the absolute path. The path is safe to store in Room — it's
     * what `AsyncImage(model = File(path))` in the gallery uses.
     *
     * Returns null on any IO failure (caller saves the entry without a photo).
     */
    private fun savePhotoLocally(entryId: String, bytes: ByteArray): String? {
        return try {
            val dir = File(ServiceLocator.appContext().filesDir, "progress_photos").apply { mkdirs() }
            val file = File(dir, "$entryId.jpg")
            FileOutputStream(file).use { out -> out.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(LogTags.STORAGE, "savePhotoLocally failed", e)
            null
        }
    }

    /** Progress banner text — resource-backed since Phase 5 Batch D2. */
    fun progressMessage(): String? {
        val s = _uiState.value
        val previous = s.previousWeightKg ?: return null
        val target = s.targetWeightKg ?: return null
        val current = s.weight.toDouble()
        val ctx = ServiceLocator.appContext()

        val deltaFromPrevious = current - previous
        if (kotlin.math.abs(deltaFromPrevious) < 0.1) {
            return ctx.getString(R.string.checkpoint_logger_maintained)
        }

        val towardGoal = when (s.goalDirection) {
            GoalDirection.LOSE -> current < previous
            GoalDirection.GAIN -> current > previous
            GoalDirection.MAINTAIN -> kotlin.math.abs(current - target) <= 0.5
        }

        val delta = kotlin.math.abs(deltaFromPrevious).toInt()
        return when {
            towardGoal && s.goalDirection == GoalDirection.LOSE ->
                ctx.getString(R.string.checkpoint_logger_closer_to_goal_format, delta)
            towardGoal && s.goalDirection == GoalDirection.GAIN ->
                ctx.getString(R.string.checkpoint_logger_closer_to_goal_format, delta)
            towardGoal -> ctx.getString(R.string.checkpoint_logger_on_track)
            else -> ctx.getString(R.string.checkpoint_logger_away_from_goal_format, delta)
        }
    }
}