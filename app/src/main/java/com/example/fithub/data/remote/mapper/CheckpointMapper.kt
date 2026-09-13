package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.CheckpointSchedule
import java.time.LocalDate
import java.time.LocalDateTime

object CheckpointMapper {
    fun toMap(c: CheckpointSchedule): Map<String, Any?> = mapOf(
        "frequencyDays" to c.frequencyDays,
        "nextCheckpointDate" to c.nextCheckpointDate.toString(),
        "remindersEnabled" to c.remindersEnabled,
        "updatedAt" to c.updatedAt.toString()
    )

    fun fromMap(id: String, uid: String, map: Map<String, Any?>): CheckpointSchedule =
        CheckpointSchedule(
            id = id,
            userId = uid,
            frequencyDays = (map["frequencyDays"] as? Number)?.toInt() ?: 14,
            nextCheckpointDate = (map["nextCheckpointDate"] as? String)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now().plusDays(14),
            remindersEnabled = map["remindersEnabled"] as? Boolean ?: true,
            updatedAt = (map["updatedAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now()
        )
}