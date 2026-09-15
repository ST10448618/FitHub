package com.example.fithub.ui.screens.food.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fithub.ml.FoodClassifier
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.PrimaryButton
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraRecognitionScreen(
    onBack: () -> Unit,
    onRecognized: (label: String, confidence: Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val classifier = remember { FoodClassifier(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember {
        mutableStateOf(
            if (classifier.isReady()) "Tap Capture to recognise your meal"
            else "Model unavailable — please add the meal manually."
        )
    }

    // Back always works, even if the previous capture threw.
    val safeBack: () -> Unit = {
        isProcessing = false
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppHeader(
            title = "AI Recognition",
            onBack = safeBack,
            titleColor = Color.White
        )

        if (!hasPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Camera permission required.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                statusText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                PrimaryButton(
                    text = "📸  Capture",
                    onClick = {
                        val capture = imageCapture
                        if (capture == null) {
                            statusText = "Camera not ready yet — try again in a moment."
                            return@PrimaryButton
                        }
                        isProcessing = true
                        statusText = "Analysing…"

                        // Write JPEG to cache dir — this is the reliable path.
                        val photoFile = File(
                            context.cacheDir,
                            "food_capture_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions
                            .Builder(photoFile)
                            .build()

                        capture.takePicture(
                            outputOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    outputFileResults: ImageCapture.OutputFileResults
                                ) {
                                    // Decode off the main thread, classify, then hop back.
                                    var prediction: FoodClassifier.Prediction? = null
                                    var err: String? = null
                                    try {
                                        val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                        if (bmp != null) {
                                            prediction = classifier.classify(bmp)
                                        } else {
                                            err = "Could not read the captured photo."
                                        }
                                    } catch (t: Throwable) {
                                        err = "Recognition failed: ${t.message ?: "unknown error"}"
                                    } finally {
                                        photoFile.delete()
                                    }

                                    val finalPrediction = prediction
                                    val finalErr = err
                                    ContextCompat.getMainExecutor(context).execute {
                                        isProcessing = false
                                        when {
                                            finalPrediction != null -> {
                                                statusText = "${finalPrediction.label} " +
                                                        "(${(finalPrediction.confidence * 100).toInt()}%)"
                                                onRecognized(
                                                    finalPrediction.label,
                                                    finalPrediction.confidence
                                                )
                                            }
                                            finalErr != null -> statusText = finalErr
                                            else -> statusText =
                                                "No model — please add the meal manually."
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    ContextCompat.getMainExecutor(context).execute {
                                        isProcessing = false
                                        statusText = "Capture failed: ${exception.message ?: "unknown"}"
                                    }
                                }
                            }
                        )
                    }
                )
            }
        }

        LaunchedEffect(Unit) {
            try {
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    try {
                        val provider = future.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (t: Throwable) {
                        statusText = "Camera unavailable: ${t.message ?: "unknown error"}"
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (t: Throwable) {
                statusText = "Camera unavailable: ${t.message ?: "unknown error"}"
            }
        }
    }
}