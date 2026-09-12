package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "checkpoint_schedule", indices = [Index("userId")])
data class CheckpointScheduleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val frequencyDays: Int,
    val nextCheckpointDate: LocalDate,
    val remindersEnabled: Boolean,
    val updatedAt: LocalDateTime
)