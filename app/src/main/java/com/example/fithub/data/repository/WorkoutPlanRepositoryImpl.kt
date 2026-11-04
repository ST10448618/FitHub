package com.example.fithub.data.repository

import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.core.fireAndForget
import com.example.fithub.data.local.dao.WorkoutPlanDao
import com.example.fithub.data.local.mapper.WorkoutPlanLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.WorkoutPlanMapper
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutPlan
import com.example.fithub.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutPlanRepositoryImpl(
    private val dao: WorkoutPlanDao,
    private val db: com.example.fithub.data.local.FitHubDatabase
) : WorkoutPlanRepository {

    private suspend fun hydrate(planId: String): WorkoutPlan? {
        val entity = dao.getById(planId) ?: return null
        val exercises = db.workoutExerciseDao().getForPlan(planId)
        return WorkoutPlanLocalMapper.toDomain(entity, exercises)
    }

    override fun observeVerified(): Flow<List<WorkoutPlan>> =
        dao.observeVerified().map { list ->
            list.map { entity ->
                val exercises = runCatching {
                    kotlinx.coroutines.runBlocking {
                        db.workoutExerciseDao().getForPlan(entity.id)
                    }
                }.getOrDefault(emptyList())
                WorkoutPlanLocalMapper.toDomain(entity, exercises)
            }
        }

    override suspend fun getVerifiedByCategory(category: WorkoutCategory): List<WorkoutPlan> {
        val plans = dao.getVerifiedByCategory(category.name)
        return plans.mapNotNull { hydrate(it.id) }
    }

    override fun observeCreated(uid: String): Flow<List<WorkoutPlan>> =
        dao.observeCreatedByUser(uid).map { list -> list.mapNotNull { hydrate(it.id) } }

    override suspend fun saveCreated(uid: String, plan: WorkoutPlan): Resource<Unit> = try {
        val entity = WorkoutPlanLocalMapper.toEntity(plan)
        dao.upsert(entity)

        db.workoutExerciseDao().deleteForPlan(plan.id)
        db.workoutExerciseDao().upsertAll(
            WorkoutPlanLocalMapper.toExerciseEntities(plan.id, plan.exercises)
        )

        // FIX: fire and forget
        FirestorePaths.createdPlan(uid, plan.id)
            .set(WorkoutPlanMapper.toMap(plan))
            .fireAndForget(LogTags.SYNC)

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save workout plan", e)
    }

    override fun observeSaved(uid: String): Flow<List<WorkoutPlan>> =
        dao.observeSavedByUser(uid).map { list -> list.mapNotNull { hydrate(it.id) } }

    override suspend fun saveFromLibrary(uid: String, plan: WorkoutPlan): Resource<Unit> = try {
        val updated = plan.copy(isSaved = true, userId = uid)
        dao.upsert(WorkoutPlanLocalMapper.toEntity(updated))

        // FIX: fire and forget
        FirestorePaths.savedPlan(uid, plan.id)
            .set(mapOf("savedAt" to java.time.LocalDateTime.now().toString()))
            .fireAndForget(LogTags.SYNC)

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save plan", e)
    }

    override suspend fun unsave(uid: String, planId: String): Resource<Unit> = try {
        val existing = dao.getById(planId)
        if (existing != null) {
            if (existing.isVerified) {
                dao.upsert(existing.copy(isSaved = false))
            } else {
                dao.delete(planId)
            }
        }
        // FIX: fire and forget
        FirestorePaths.savedPlan(uid, planId).delete().fireAndForget(LogTags.SYNC)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to unsave plan", e)
    }

    override fun observeById(planId: String): Flow<WorkoutPlan?> =
        dao.observeById(planId).map { entity ->
            entity?.let {
                val exercises = runCatching {
                    kotlinx.coroutines.runBlocking {
                        db.workoutExerciseDao().getForPlan(it.id)
                    }
                }.getOrDefault(emptyList())
                WorkoutPlanLocalMapper.toDomain(it, exercises)
            }
        }

    override suspend fun getById(planId: String): WorkoutPlan? = hydrate(planId)

    override suspend fun syncVerifiedFromRemote(): Resource<Unit> = try {
        val snapshot = FirestorePaths.verifiedWorkoutPlans.get().await()
        snapshot.documents.forEach { doc ->
            doc.data?.let { map ->
                val plan = WorkoutPlanMapper.fromMap(doc.id, map)
                dao.upsert(WorkoutPlanLocalMapper.toEntity(plan))
                db.workoutExerciseDao().deleteForPlan(plan.id)
                db.workoutExerciseDao().upsertAll(
                    WorkoutPlanLocalMapper.toExerciseEntities(plan.id, plan.exercises)
                )
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync verified plans", e)
    }
}