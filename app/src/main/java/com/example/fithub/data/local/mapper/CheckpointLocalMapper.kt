package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.CheckpointScheduleEntity
import com.example.fithub.domain.model.CheckpointSchedule

object CheckpointLocalMapper {
    fun toDomain(e: CheckpointScheduleEntity) = CheckpointSchedule(
        id = e.id,
        userId = e.userId,
        frequencyDays = e.frequencyDays,
        nextCheckpointDate = e.nextCheckpointDate,
        remindersEnabled = e.remindersEnabled,
        updatedAt = e.updatedAt
    )

    fun toEntity(d: CheckpointSchedule) = CheckpointScheduleEntity(
        id = d.id,
        userId = d.userId,
        frequencyDays = d.frequencyDays,
        nextCheckpointDate = d.nextCheckpointDate,
        remindersEnabled = d.remindersEnabled,
        updatedAt = d.updatedAt
    )
}