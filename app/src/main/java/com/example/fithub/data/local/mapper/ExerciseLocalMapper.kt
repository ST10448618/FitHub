package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.ExerciseEntity
import com.example.fithub.domain.model.Exercise

object ExerciseLocalMapper {
    fun toDomain(e: ExerciseEntity) = Exercise(
        id = e.id,
        name = e.name,
        category = e.category,
        description = e.description,
        difficulty = e.difficulty,
        equipment = e.equipment,
        isTimeBased = e.isTimeBased,
        estimatedActivityPerRep = e.estimatedActivityPerRep,
        estimatedActivityPerSecond = e.estimatedActivityPerSecond,
        estimatedSecondsPerRep = e.estimatedSecondsPerRep,
        imageUrl = e.imageUrl
    )

    fun toEntity(d: Exercise) = ExerciseEntity(
        id = d.id,
        name = d.name,
        category = d.category,
        description = d.description,
        difficulty = d.difficulty,
        equipment = d.equipment,
        isTimeBased = d.isTimeBased,
        estimatedActivityPerRep = d.estimatedActivityPerRep,
        estimatedActivityPerSecond = d.estimatedActivityPerSecond,
        estimatedSecondsPerRep = d.estimatedSecondsPerRep,
        imageUrl = d.imageUrl
    )
}