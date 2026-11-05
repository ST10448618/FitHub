// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A single recorded weigh-in.
 * current weight is always derived from the latest WeightEntry.
 *
 * [photoUrl] is the Firebase Storage download URL for an optional progress
 * photo captured at log time. Null means no photo was attached.
 */
data class WeightEntry(
    val id: String,
    val userId: String,
    val weightKg: Double,
    val date: LocalDate,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val photoUrl: String? = null
)