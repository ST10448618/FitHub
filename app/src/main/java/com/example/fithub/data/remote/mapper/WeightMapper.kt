package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.WeightEntry
import java.time.LocalDate
import java.time.LocalDateTime

object WeightMapper {

    fun toMap(w: WeightEntry): Map<String, Any?> = mapOf(
        "weightKg" to w.weightKg,
        "date" to w.date.toString(),
        "note" to w.note,
        "createdAt" to w.createdAt.toString()
    )

    fun fromMap(id: String, userId: String, map: Map<String, Any?>): WeightEntry =
        WeightEntry(
            id = id,
            userId = userId,
            weightKg = (map["weightKg"] as? Number)?.toDouble() ?: 0.0,
            date = (map["date"] as? String)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now(),
            note = map["note"] as? String,
            createdAt = (map["createdAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now()
        )
}