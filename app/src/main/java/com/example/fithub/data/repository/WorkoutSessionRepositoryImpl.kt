package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.FitHubDatabase
import com.example.fithub.data.local.dao.WorkoutSessionDao
import com.example.fithub.data.local.mapper.WorkoutSessionLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.WorkoutSessionMapper
import com.example.fithub.domain.model.WorkoutSession
import com.example.fithub.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkoutSessionRepositoryImpl(
    private val dao: WorkoutSessionDao,
    private val db: FitHubDatabase
) : WorkoutSessionRepository {

    private suspend fun hydrate(id: String): WorkoutSession? {
        val entity = dao.getById(id) ?: return null
        val ex = db.completedExerciseDao().getForSession(id)
        return WorkoutSessionLocalMapper.toDomain(entity, ex)
    }

    override fun observeCompleted(uid: String): Flow<List<WorkoutSession>> =
        dao.observeCompleted(uid).map { list -> list.mapNotNull { hydrate(it.id) } }

    override fun observeByDate(uid: String, date: LocalDate): Flow<List<WorkoutSession>> =
        dao.observeByDate(uid, date.toString()).map { list ->
            list.mapNotNull { hydrate(it.id) }
        }

    override fun observeRecent(uid: String, limit: Int): Flow<List<WorkoutSession>> =
        dao.observeRecent(uid, limit).map { list -> list.mapNotNull { hydrate(it.id) } }

    override suspend fun getBetween(uid: String, from: LocalDate, to: LocalDate): List<WorkoutSession> =
        dao.getBetween(uid, from.toString(), to.toString()).mapNotNull { hydrate(it.id) }

    override suspend fun countBetween(uid: String, from: LocalDate, to: LocalDate): Int =
        dao.countBetween(uid, from.toString(), to.toString())

    override suspend fun save(session: WorkoutSession): Resource<Unit> = try {
        dao.upsert(WorkoutSessionLocalMapper.toEntity(session))
        db.completedExerciseDao().deleteForSession(session.id)
        db.completedExerciseDao().upsertAll(
            WorkoutSessionLocalMapper.toExerciseEntities(session.id, session.exercises)
        )
        FirestorePaths.workoutSession(session.userId, session.id)
            .set(WorkoutSessionMapper.toMap(session))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save workout session", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.workoutSessions(uid).get().await()
        snapshot.documents.forEach { doc ->
            doc.data?.let { map ->
                val session = WorkoutSessionMapper.fromMap(doc.id, uid, map)
                dao.upsert(WorkoutSessionLocalMapper.toEntity(session))
                db.completedExerciseDao().deleteForSession(session.id)
                db.completedExerciseDao().upsertAll(
                    WorkoutSessionLocalMapper.toExerciseEntities(session.id, session.exercises)
                )
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync workout sessions", e)
    }
}