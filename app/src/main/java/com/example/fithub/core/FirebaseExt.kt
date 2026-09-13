package com.example.fithub.core

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