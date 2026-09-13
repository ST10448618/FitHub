package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.WorkoutGoalsDao
import com.example.fithub.data.local.mapper.WorkoutGoalsLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.WorkoutGoalsMapper
import com.example.fithub.domain.model.WorkoutGoals
import com.example.fithub.domain.repository.WorkoutGoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutGoalsRepositoryImpl(
    private val dao: WorkoutGoalsDao
) : WorkoutGoalsRepository {

    override fun observe(uid: String): Flow<WorkoutGoals?> =
        dao.observe(uid).map { it?.let(WorkoutGoalsLocalMapper::toDomain) }

    override suspend fun get(uid: String): WorkoutGoals? =
        dao.get(uid)?.let(WorkoutGoalsLocalMapper::toDomain)

    override suspend fun save(goals: WorkoutGoals): Resource<Unit> = try {
        dao.upsert(WorkoutGoalsLocalMapper.toEntity(goals))
        FirestorePaths.workoutGoal(goals.userId, goals.id)
            .set(WorkoutGoalsMapper.toMap(goals))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save workout goals", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.workoutGoals(uid).limit(1).get().await()
        snapshot.documents.firstOrNull()?.let { doc ->
            doc.data?.let { map ->
                val goals = WorkoutGoalsMapper.fromMap(doc.id, uid, map)
                dao.upsert(WorkoutGoalsLocalMapper.toEntity(goals))
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync workout goals", e)
    }
}