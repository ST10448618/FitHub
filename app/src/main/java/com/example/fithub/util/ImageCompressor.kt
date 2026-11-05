// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.fithub.core.LogTags
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.math.max

/**
 * Downscales and re-encodes user-supplied images before upload.
 *
 * Why not the raw camera output?
 *  - Modern phone cameras produce 4-12 MB JPEGs (or HEIC), which exceed the
 *    5 MB Storage rule and slow uploads on mobile data.
 *  - EXIF orientations are often wrong on raw files; photos can appear
 *    sideways unless the rotation is applied.
 *
 * Contract: `compress(uri)` returns JPEG bytes at max 1080 px on the long
 * edge, quality 80, EXIF-corrected. Typically 100-250 KB.
 *
 * References:
 *  - https://developer.android.com/topic/performance/graphics/load-bitmap
 *  - https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface
 * (accessed February 2026)
 */
object ImageCompressor {

    private const val MAX_LONG_EDGE_PX = 1080
    private const val JPEG_QUALITY = 80

    fun compress(context: Context, uri: Uri): ByteArray? = try {
        val decoded: Bitmap? = decodeDownsampled(context, uri)
        if (decoded == null) {
            Log.w(LogTags.STORAGE, "compress: could not decode $uri")
            null
        } else {
            val rotated = applyExifRotation(context, uri, decoded)
            val out = ByteArrayOutputStream()
            rotated.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            if (rotated !== decoded) rotated.recycle()
            decoded.recycle()
            Log.i(LogTags.STORAGE, "compress ok size=${out.size() / 1024}KB")
            out.toByteArray()
        }
    } catch (t: Throwable) {
        Log.e(LogTags.STORAGE, "compress failed", t)
        null
    }

    fun saveBytesToPendingDir(context: Context, entryId: String, bytes: ByteArray): File {
        val dir = File(context.filesDir, "pending_photos").apply { mkdirs() }
        val file = File(dir, "$entryId.jpg")
        file.writeBytes(bytes)
        Log.i(LogTags.STORAGE, "pending photo saved to ${file.absolutePath}")
        return file
    }

    fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        maxLongEdge: Int = MAX_LONG_EDGE_PX
    ): Int {
        if (rawWidth <= 0 || rawHeight <= 0) return 1
        val longEdge = max(rawWidth, rawHeight)
        if (longEdge <= maxLongEdge) return 1
        var sample = 1
        while (longEdge / (sample * 2) >= maxLongEdge) sample *= 2
        return sample
    }

    private fun decodeDownsampled(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
        } catch (t: Throwable) {
            Log.w(LogTags.STORAGE, "decodeDownsampled bounds failed", t)
            return null
        }
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }

        val stream: InputStream = try {
            context.contentResolver.openInputStream(uri)
        } catch (t: Throwable) {
            Log.w(LogTags.STORAGE, "decodeDownsampled openInputStream failed", t)
            null
        } ?: return null

        return try {
            stream.use { BitmapFactory.decodeStream(it, null, opts) }
        } catch (t: Throwable) {
            Log.w(LogTags.STORAGE, "decodeDownsampled decodeStream failed", t)
            null
        }
    }

    /**
     * Returns a rotated copy of [bitmap] if EXIF says so, otherwise returns
     * [bitmap] itself. **Never returns null** so the caller can recycle
     * confidently.
     */
    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (t: Throwable) {
            Log.w(LogTags.STORAGE, "applyExifRotation: EXIF read failed", t)
            null
        } ?: ExifInterface.ORIENTATION_NORMAL

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap

        return try {
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (t: Throwable) {
            Log.w(LogTags.STORAGE, "applyExifRotation: rotate failed, using unrotated", t)
            bitmap
        }
    }
}