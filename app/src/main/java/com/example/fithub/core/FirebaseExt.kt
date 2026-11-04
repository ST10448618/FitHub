package com.example.fithub.core

import android.util.Log
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends until the given Firebase Task completes.
 * Throw on failure, return result on success.
 */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWithException(task.exception ?: RuntimeException("Firebase task failed"))
        }
    }
    cont.invokeOnCancellation { /* can't cancel Firebase tasks */ }
}


/**
 * Executes a Firestore task without suspending the coroutine.
 * Perfect for offline-first "fire and forget" writes where Firestore's local
 * cache will handle the sync when the device reconnects.
 */
fun Task<*>.fireAndForget(logTag: String) {
    addOnFailureListener { e ->
        Log.e(logTag, "Background sync failed, queued locally by Firestore cache", e)
    }
    // We do not await() here, so the UI thread is never blocked!
}