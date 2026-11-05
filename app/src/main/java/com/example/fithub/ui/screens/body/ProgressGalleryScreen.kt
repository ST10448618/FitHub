package com.example.fithub.ui.screens.body

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.R
import com.example.fithub.core.i18n.LocaleManager
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Phase 6B (revised): resolves a `photoUrl` string to either a [File] (local
 * path) or a String (legacy https URL). Coil supports both — this helper just
 * prevents the local path from being interpreted as a malformed URI.
 */
private fun photoModel(url: String): Any =
    if (url.startsWith("/")) File(url) else url

@Composable
fun ProgressGalleryScreen(
    onBack: () -> Unit,
    viewModel: ProgressGalleryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val locale = Locale.forLanguageTag(LocaleManager.currentCode(context))
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    var fullScreenPhotoUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.progress_gallery_title), onBack = onBack)

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.progress_gallery_empty_title),
                    message = stringResource(R.string.progress_gallery_empty_body),
                    emoji = "\uD83D\uDCF8" // 📸
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Before vs Now comparison card
            state.before?.let { before ->
                state.now?.let { now ->
                    RoundedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.progress_gallery_compare_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CompareColumn(
                                label = stringResource(R.string.progress_gallery_before),
                                weightKg = before.weightKg,
                                photoUrl = before.photoUrl,
                                dateText = before.date.format(dateFormatter),
                                modifier = Modifier.weight(1f)
                            )
                            CompareColumn(
                                label = stringResource(R.string.progress_gallery_now),
                                weightKg = now.weightKg,
                                photoUrl = now.photoUrl,
                                dateText = now.date.format(dateFormatter),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        state.deltaKg?.let { delta ->
                            val sign = if (delta >= 0) "+" else ""
                            Text(
                                stringResource(
                                    R.string.progress_gallery_delta_format,
                                    "$sign${"%.1f".format(locale, delta)}"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Text(
                stringResource(R.string.progress_gallery_all_photos),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.photos) { photo ->
                    PhotoTile(
                        photo = photo,
                        dateText = photo.date.format(dateFormatter),
                        onClick = { fullScreenPhotoUrl = photo.photoUrl }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    fullScreenPhotoUrl?.let { url ->
        Dialog(onDismissRequest = { fullScreenPhotoUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardWhite)
            ) {
                AsyncImage(
                    model = photoModel(url),
                    contentDescription = stringResource(R.string.progress_gallery_photo_desc),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CompareColumn(
    label: String,
    weightKg: Double,
    photoUrl: String?,
    dateText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGray)
        ) {
            AsyncImage(
                model = photoUrl?.let(::photoModel),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.progress_gallery_kg_at_date, weightKg.toInt(), dateText),
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun PhotoTile(
    photo: ProgressGalleryViewModel.PhotoTile,
    dateText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = photoModel(photo.photoUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                stringResource(R.string.progress_gallery_kg_format, photo.weightKg.toInt()),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                dateText,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}