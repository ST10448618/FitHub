package com.example.fithub.ui.screens.body

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.R
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*


@Composable
fun CheckpointLoggerScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CheckpointLoggerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Photo picker: no storage permission needed (uses the system photo picker).
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.attachPhoto(uri)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.checkpoint_logger_title), onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u25CE", // ◉
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.checkpoint_logger_hero_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.checkpoint_logger_hero_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.checkpoint_logger_current_weight),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            NumberStepper(
                value = state.weight,
                onValueChange = viewModel::onWeightChange,
                step = 1,
                min = 20,
                max = 400,
                unit = stringResource(R.string.common_unit_kg)
            )

            Spacer(Modifier.height(20.dp))

            // ---- Progress photo picker (Phase 6 / UDF 4) ----
            Text(
                stringResource(R.string.checkpoint_logger_photo_title),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.checkpoint_logger_photo_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))

            if (state.pendingPhotoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                ) {
                    AsyncImage(
                        model = state.pendingPhotoUri,
                        contentDescription = stringResource(R.string.checkpoint_logger_photo_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { viewModel.clearPhoto() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            stringResource(R.string.checkpoint_logger_photo_remove),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // No progress bar: the local copy is instantaneous. Just
                // confirm the photo is attached and will be saved with the log.
                Text(
                    stringResource(R.string.checkpoint_logger_photo_attached_local),
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                        .border(1.dp, SurfaceGray, RoundedCornerShape(16.dp))
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.checkpoint_logger_photo_pick),
                        style = MaterialTheme.typography.labelMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            viewModel.progressMessage()?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FitHubLightBlue, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\u24D8", // ⓘ
                            style = MaterialTheme.typography.titleLarge,
                            color = FitHubPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                stringResource(R.string.checkpoint_logger_progress_detected),
                                style = MaterialTheme.typography.labelMedium,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                message,
                                style = MaterialTheme.typography.bodySmall,
                                color = FitHubPrimary
                            )
                        }
                    }
                }
            }

            state.errorMessage?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(err, color = ErrorRed)
            }

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.generic_cancel),
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = stringResource(
                        if (state.isSaving) R.string.checkpoint_logger_logging
                        else R.string.checkpoint_logger_log_button
                    ),
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}