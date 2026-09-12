package com.example.fithub.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Configuration for when the user should record a weigh-in.
 * Actual weigh-ins live in WeightEntry.
 */
data class CheckpointSchedule(
    val id: String,
    val userId: String,
    val frequencyDays: Int = 14,
    val nextCheckpointDate: LocalDate = LocalDate.now().plusDays(14),
    val remindersEnabled: Boolean = true,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)