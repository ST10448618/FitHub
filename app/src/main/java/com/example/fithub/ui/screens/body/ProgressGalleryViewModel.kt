package com.example.fithub.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        /** Absolute local path, e.g. `/data/user/0/com.example.fithub/files/progress_photos/xyz.jpg`. */
        val photoUrl: String,
        val weightKg: Double,
        val date: LocalDate
    )

    private val uid = SessionManager.currentUserId ?: ""
    private val weightRepo = ServiceLocator.weightRepository

    private val _uiState = MutableStateFlow(ProgressGalleryUiState())
    val uiState: StateFlow<ProgressGalleryUiState> = _uiState.asStateFlow()

    init { observe() }

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
}