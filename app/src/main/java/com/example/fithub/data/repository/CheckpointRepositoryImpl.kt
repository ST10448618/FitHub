package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.CheckpointScheduleDao
import com.example.fithub.data.local.mapper.CheckpointLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.CheckpointMapper
import com.example.fithub.domain.model.CheckpointSchedule
import com.example.fithub.domain.repository.CheckpointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckpointRepositoryImpl(
    private val dao: CheckpointScheduleDao
) : CheckpointRepository {

    override fun observe(uid: String): Flow<CheckpointSchedule?> =
        dao.observe(uid).map { it?.let(CheckpointLocalMapper::toDomain) }

    override suspend fun get(uid: String): CheckpointSchedule? =
        dao.get(uid)?.let(CheckpointLocalMapper::toDomain)

    override suspend fun save(schedule: CheckpointSchedule): Resource<Unit> = try {
        dao.deleteAllForUser(schedule.userId)
        dao.upsert(CheckpointLocalMapper.toEntity(schedule))
        FirestorePaths.checkpointSchedule(schedule.userId, schedule.id)
            .set(CheckpointMapper.toMap(schedule))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save checkpoint schedule", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.checkpointSchedules(uid).limit(1).get().await()
        snapshot.documents.firstOrNull()?.let { doc ->
            doc.data?.let { map ->
                val schedule = CheckpointMapper.fromMap(doc.id, uid, map)
                dao.upsert(CheckpointLocalMapper.toEntity(schedule))
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync checkpoint schedule", e)
    }
}