package com.example.fithub.data.repository

import com.example.fithub.data.local.dao.ExerciseDao
import com.example.fithub.data.local.mapper.ExerciseLocalMapper
import com.example.fithub.domain.model.Exercise
import com.example.fithub.domain.model.ExerciseCategory
import com.example.fithub.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val dao: ExerciseDao
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        dao.observeAll().map { list -> list.map(ExerciseLocalMapper::toDomain) }

    override suspend fun getByCategory(category: ExerciseCategory): List<Exercise> =
        dao.getByCategory(category.name).map(ExerciseLocalMapper::toDomain)

    override suspend fun search(query: String): List<Exercise> =
        dao.search(query).map(ExerciseLocalMapper::toDomain)

    override suspend fun getById(id: String): Exercise? =
        dao.getById(id)?.let(ExerciseLocalMapper::toDomain)
}