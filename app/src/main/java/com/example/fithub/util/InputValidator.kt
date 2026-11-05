package com.example.fithub.util

object InputValidator {
    // Pure Kotlin regex - 100% JVM testable, no Android framework dependencies!
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidWeight(weight: String): Boolean {
        val w = weight.toDoubleOrNull() ?: return false
        return w in 20.0..300.0
    }
}