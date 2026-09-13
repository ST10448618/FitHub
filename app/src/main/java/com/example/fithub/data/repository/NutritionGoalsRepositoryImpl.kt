package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.NutritionGoalsDao
import com.example.fithub.data.local.mapper.NutritionGoalsLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.NutritionGoalsMapper
import com.example.fithub.domain.model.NutritionGoals
import com.example.fithub.domain.repository.NutritionGoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NutritionGoalsRepositoryImpl(
    private val dao: NutritionGoalsDao
) : NutritionGoalsRepository {

    override fun observeCurrent(uid: String): Flow<NutritionGoals?> =
        dao.observeCurrent(uid).map { it?.let(NutritionGoalsLocalMapper::toDomain) }

    override suspend fun getCurrent(uid: String): NutritionGoals? =
        dao.getCurrent(uid)?.let(NutritionGoalsLocalMapper::toDomain)

    override suspend fun save(goals: NutritionGoals): Resource<Unit> = try {
        dao.upsert(NutritionGoalsLocalMapper.toEntity(goals))
        FirestorePaths.nutritionGoal(goals.userId, goals.id)
            .set(NutritionGoalsMapper.toMap(goals))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save nutrition goals", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.nutritionGoals(uid)
            .orderBy("effectiveDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.let { doc ->
            doc.data?.let { map ->
                val goals = NutritionGoalsMapper.fromMap(doc.id, uid, map)
                dao.upsert(NutritionGoalsLocalMapper.toEntity(goals))
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync nutrition goals", e)
    }
}