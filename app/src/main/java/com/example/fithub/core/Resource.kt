package com.example.fithub.core

/**
 * Wrapper for async operations that can fail.
 * Used by repositories so the UI can render loading / error / success uniformly.
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
}

/** Convenience: run a suspending block and wrap the result. */
suspend fun <T> runResource(block: suspend () -> T): Resource<T> =
    try {
        Resource.Success(block())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error", e)
    }