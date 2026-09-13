package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.data.remote.auth.FirebaseAuthDataSource
import com.example.fithub.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val auth: FirebaseAuthDataSource = FirebaseAuthDataSource()
) : AuthRepository {

    override fun currentUserId(): String? = auth.currentUserId()

    override fun observeAuthState(): Flow<String?> = auth.observeAuthState()

    override suspend fun register(email: String, password: String): Resource<String> =
        try {
            val user = auth.createUser(email.trim(), password)
                ?: return Resource.Error("Registration failed — no user returned.")
            Resource.Success(user.uid)
        } catch (e: Exception) {
            Resource.Error(mapAuthError(e), e)
        }

    override suspend fun login(email: String, password: String): Resource<String> =
        try {
            val user = auth.signIn(email.trim(), password)
                ?: return Resource.Error("Login failed — no user returned.")
            Resource.Success(user.uid)
        } catch (e: Exception) {
            Resource.Error(mapAuthError(e), e)
        }

    override suspend fun logout(): Resource<Unit> = try {
        auth.signOut()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Sign-out failed", e)
    }

    override suspend fun resetPassword(email: String): Resource<Unit> = try {
        auth.sendPasswordReset(email.trim())
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(mapAuthError(e), e)
    }

    /** Friendly error messages — never leak Firebase internals to the UI. */
    private fun mapAuthError(e: Exception): String {
        val msg = e.message ?: return "Authentication failed."
        return when {
            msg.contains("password is invalid", ignoreCase = true) -> "Incorrect email or password."
            msg.contains("no user record", ignoreCase = true) -> "No account found with that email."
            msg.contains("email address is already in use", ignoreCase = true) -> "That email is already registered."
            msg.contains("network", ignoreCase = true) -> "Network error — check your connection."
            msg.contains("badly formatted", ignoreCase = true) -> "Please enter a valid email address."
            msg.contains("at least 6 characters", ignoreCase = true) -> "Password must be at least 6 characters."
            else -> msg
        }
    }
}