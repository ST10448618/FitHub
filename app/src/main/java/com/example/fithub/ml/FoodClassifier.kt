package com.example.fithub.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * On-device food classifier using a TFLite model exported from Teachable Machine.
 *
 * If `app/src/main/assets/food_model.tflite` is missing, `isReady()` returns false
 * and the caller must fall back to the online API.
 */
class FoodClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    init {
        try {
            val model = loadModelFile("food_model.tflite")
            interpreter = Interpreter(model)
            labels = context.assets.open("food_labels.txt")
                .bufferedReader()
                .readLines()
                .map { it.substringAfter(" ", "").trim() }
                .filter { it.isNotEmpty() }
        } catch (t: Throwable) {
            interpreter = null
            labels = emptyList()
        }
    }

    fun isReady(): Boolean = interpreter != null && labels.isNotEmpty()

    data class Prediction(val label: String, val confidence: Float)

    fun classify(bitmap: Bitmap): Prediction? {
        val itp = interpreter ?: return null
        if (labels.isEmpty()) return null

        val inputSize = 224
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val input = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply {
            order(ByteOrder.nativeOrder())
            val pixels = IntArray(inputSize * inputSize)
            resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            pixels.forEach { px ->
                val r = ((px shr 16) and 0xFF) / 255f
                val g = ((px shr 8) and 0xFF) / 255f
                val b = (px and 0xFF) / 255f
                putFloat(r); putFloat(g); putFloat(b)
            }
            rewind()
        }

        val output = Array(1) { FloatArray(labels.size) }
        itp.run(input, output)
        val scores = output[0]

        var bestIdx = 0
        var best = scores[0]
        for (i in scores.indices) if (scores[i] > best) { best = scores[i]; bestIdx = i }

        return Prediction(
            label = labels.getOrElse(bestIdx) { "Unknown" },
            confidence = best
        )
    }

    private fun loadModelFile(name: String): ByteBuffer {
        val fd = context.assets.openFd(name)
        FileInputStream(fd.fileDescriptor).use { fis ->
            val channel = fis.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }
}