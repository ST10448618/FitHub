package com.example.fithub.domain.model

import java.time.LocalDateTime

/**
 * The user's in-app currency balance (Particles).
 */
data class ParticleBalance(
    val userId: String,
    val balance: Int = 0,
    val lifetimeEarned: Int = 0,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)