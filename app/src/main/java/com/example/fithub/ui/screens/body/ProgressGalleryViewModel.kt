package com.example.fithub.ui.screens.body

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.util.ImageCompressor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

data class ProgressGalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<ProgressGalleryViewModel.PhotoTile> = emptyList(),
    val before: ProgressGalleryViewModel.PhotoTile? = null,
    val now: ProgressGalleryViewModel.PhotoTile? = null,
    val deltaKg: Double? = null,
    val errorMessage: String? = null
)

class ProgressGalleryViewModel : ViewModel() {

    data class PhotoTile(
        val id: String,
        val photoUrl: String,
        val weightKg: Double,
        val date: LocalDate
    )

    private val uid = SessionManager.currentUserId ?: ""
    private val weightRepo = ServiceLocator.weightRepository
    private val storage = ServiceLocator.storageDataSource
    private val isOnlineFlow = ServiceLocator.connectivityMonitor.isOnline

    private val _uiState = MutableStateFlow(ProgressGalleryUiState())
    val uiState: StateFlow<ProgressGalleryUiState> = _uiState.asStateFlow()

    init {
        // 1. Try to flush any pending offline photos first.
        viewModelScope.launch { retryPendingPhotos() }
        // 2. Then observe the gallery.
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            weightRepo.observeAll(uid).collect { all ->
                val photos = all
                    .filter { !it.photoUrl.isNullOrBlank() }
                    .sortedBy { it.date }
                    .map {
                        PhotoTile(
                            id = it.id,
                            photoUrl = it.photoUrl!!,
                            weightKg = it.weightKg,
                            date = it.date
                        )
                    }
                val before = photos.firstOrNull()
                val now = photos.lastOrNull()
                val delta = if (photos.size >= 2) now!!.weightKg - before!!.weightKg else null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        photos = photos,
                        before = before,
                        now = now,
                        deltaKg = delta
                    )
                }
            }
        }
    }

    /**
     * If the user logged a weigh-in while offline, its compressed photo lives
     * at `filesDir/pending_photos/{entryId}.jpg`. If we're online now, upload
     * those, update the row's `photoUrl`, delete the local file.
     */
    private suspend fun retryPendingPhotos() {
        if (!isOnlineFlow.value) return
        val pendingDir = File(ServiceLocator.appContext().filesDir, "pending_photos")
        val files = pendingDir.listFiles()?.filter { it.extension == "jpg" } ?: return
        if (files.isEmpty()) return

        for (file in files) {
            val entryId = file.nameWithoutExtension
            val bytes = file.readBytes()
            when (val result = storage.uploadProgressPhoto(uid, entryId, bytes)) {
                is Resource.Success -> {
                    weightRepo.updatePhotoUrl(entryId, result.data)
                    file.delete()
                    Log.i(LogTags.STORAGE, "pending photo uploaded for $entryId")
                }
                is Resource.Error -> Log.w(LogTags.STORAGE, "retry upload failed for $entryId: ${result.message}")
                Resource.Loading -> Unit
            }
        }
    }
}