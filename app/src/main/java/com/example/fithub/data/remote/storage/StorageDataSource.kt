// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.remote.storage

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Firebase Storage access layer.
//
// Paths used:
//   - Progress photo per weigh-in : users/{uid}/progress/{entryId}.jpg
//   - Profile picture             : users/{uid}/profile/avatar.jpg
//
// Progress callbacks are delivered from Firebase's own worker thread; UI
// callers (Compose) collect them through LaunchedEffect / remember state.
//
// Security rules (Firebase Console, Storage -> Rules):
//   Only the owner may read or write under users/{userId}/**, uploads are
//   capped at 5 MB and image/* MIME.
//
// References:
//   - https://firebase.google.com/docs/storage/android/upload-files
//   - https://firebase.google.com/docs/storage/security
// (accessed February 2026)
class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    // Uploads bytes to users/{uid}/progress/{entryId}.jpg.
    // Returns Resource.Success with the download URL string on success.
    suspend fun uploadProgressPhoto(
        uid: String,
        entryId: String,
        bytes: ByteArray,
        onProgress: (Double) -> Unit = {}
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val ref = referenceFor(uid, entryId)
            upload(ref, bytes, onProgress)
        } catch (t: Throwable) {
            Log.e(LogTags.STORAGE, "uploadProgressPhoto failed", t)
            Resource.Error(t.message ?: "Upload failed", t)
        }
    }

    // Uploads bytes as users/{uid}/profile/avatar.jpg, overwriting any
    // previous avatar. Returns Resource.Success with the download URL.
    suspend fun uploadProfilePhoto(
        uid: String,
        bytes: ByteArray,
        onProgress: (Double) -> Unit = {}
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val ref = storage.reference
                .child("users")
                .child(uid)
                .child("profile")
                .child("avatar.jpg")
            upload(ref, bytes, onProgress)
        } catch (t: Throwable) {
            Log.e(LogTags.STORAGE, "uploadProfilePhoto failed", t)
            Resource.Error(t.message ?: "Upload failed", t)
        }
    }

    // Deletes the progress photo for entryId if it exists.
    // Treats "object not found" as success.
    suspend fun deleteProgressPhoto(uid: String, entryId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                referenceFor(uid, entryId).delete().await()
                Resource.Success(Unit)
            } catch (t: Throwable) {
                Log.w(LogTags.STORAGE, "deleteProgressPhoto no-op/failure: ${t.message}")
                Resource.Success(Unit)
            }
        }

    private fun referenceFor(uid: String, entryId: String): StorageReference =
        storage.reference
            .child("users")
            .child(uid)
            .child("progress")
            .child("$entryId.jpg")

    private suspend fun upload(
        ref: StorageReference,
        bytes: ByteArray,
        onProgress: (Double) -> Unit
    ): Resource<String> {
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        val task = ref.putBytes(bytes, metadata)
        task.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount
            if (total > 0) {
                onProgress(snapshot.bytesTransferred.toDouble() / total.toDouble())
            }
        }
        task.await()
        val url = ref.downloadUrl.await()
        Log.i(LogTags.STORAGE, "upload ok -> ${url.host}${url.path?.take(32) ?: ""}")
        return Resource.Success(url.toString())
    }
}