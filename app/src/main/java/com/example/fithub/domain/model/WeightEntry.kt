package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A single recorded weigh-in.
 * current weight is always derived from the latest WeightEntry.
 */
data class WeightEntry(
    val id: String,
    val userId: String,
    val weightKg: Double,
    val date: LocalDate,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)