// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.WeightEntryEntity
import com.example.fithub.domain.model.WeightEntry

object WeightLocalMapper {
    fun toDomain(e: WeightEntryEntity) = WeightEntry(
        id = e.id,
        userId = e.userId,
        weightKg = e.weightKg,
        date = e.date,
        note = e.note,
        createdAt = e.createdAt,
        photoUrl = e.photoUrl
    )

    fun toEntity(d: WeightEntry) = WeightEntryEntity(
        id = d.id,
        userId = d.userId,
        weightKg = d.weightKg,
        date = d.date,
        note = d.note,
        createdAt = d.createdAt,
        photoUrl = d.photoUrl
    )
}