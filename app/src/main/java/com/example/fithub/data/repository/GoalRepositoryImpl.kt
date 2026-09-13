package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.GoalDao
import com.example.fithub.data.local.mapper.GoalLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.GoalMapper
import com.example.fithub.domain.model.Goal
import com.example.fithub.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl(
    private val dao: GoalDao
) : GoalRepository {

    override fun observeCurrent(uid: String): Flow<Goal?> =
        dao.observeCurrent(uid).map { it?.let(GoalLocalMapper::toDomain) }

    override suspend fun getCurrent(uid: String): Goal? =
        dao.getCurrent(uid)?.let(GoalLocalMapper::toDomain)

    override suspend fun setGoal(goal: Goal): Resource<Unit> = try {
        dao.upsert(GoalLocalMapper.toEntity(goal))
        FirestorePaths.goal(goal.userId, goal.id)
            .set(GoalMapper.toMap(goal))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save goal", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.goals(uid).get().await()
        val goals = snapshot.documents.mapNotNull { doc ->
            val map = doc.data ?: return@mapNotNull null
            GoalMapper.fromMap(doc.id, uid, map)
        }
        goals.forEach { dao.upsert(GoalLocalMapper.toEntity(it)) }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync goals", e)
    }
}