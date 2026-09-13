package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Authentication abstraction. Firebase impl lives in the data layer.
 */
interface AuthRepository {
    /** Currently authenticated user ID or null. */
    fun currentUserId(): String?

    /** Observe authentication state — emits uid (or null when logged out). */
    fun observeAuthState(): Flow<String?>

    suspend fun register(email: String, password: String): Resource<String>

    suspend fun login(email: String, password: String): Resource<String>

    suspend fun logout(): Resource<Unit>

    suspend fun resetPassword(email: String): Resource<Unit>
}