package com.example.fithub.domain.repository

import com.example.fithub.domain.model.Exercise
import com.example.fithub.domain.model.ExerciseCategory
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeAll(): Flow<List<Exercise>>
    suspend fun getByCategory(category: ExerciseCategory): List<Exercise>
    suspend fun search(query: String): List<Exercise>
    suspend fun getById(id: String): Exercise?
}