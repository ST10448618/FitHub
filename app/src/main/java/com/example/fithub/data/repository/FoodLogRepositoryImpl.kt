package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.FoodLogDao
import com.example.fithub.data.local.mapper.FoodLogLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.FoodLogMapper
import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.repository.FoodLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FoodLogRepositoryImpl(
    private val dao: FoodLogDao
) : FoodLogRepository {

    override fun observeByDate(uid: String, date: LocalDate): Flow<List<FoodLog>> =
        dao.observeByDate(uid, date.toString()).map { list ->
            list.map(FoodLogLocalMapper::toDomain)
        }

    override fun observeRecent(uid: String, limit: Int): Flow<List<FoodLog>> =
        dao.observeRecent(uid, limit).map { list ->
            list.map(FoodLogLocalMapper::toDomain)
        }

    override suspend fun getByDate(uid: String, date: LocalDate): List<FoodLog> =
        dao.getByDate(uid, date.toString()).map(FoodLogLocalMapper::toDomain)

    override suspend fun getBetween(uid: String, from: LocalDate, to: LocalDate): List<FoodLog> =
        dao.getBetween(uid, from.toString(), to.toString()).map(FoodLogLocalMapper::toDomain)

    override suspend fun add(log: FoodLog): Resource<Unit> = try {
        dao.upsert(FoodLogLocalMapper.toEntity(log))
        FirestorePaths.foodLog(log.userId, log.id)
            .set(FoodLogMapper.toMap(log))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save food log", e)
    }

    override suspend fun delete(log: FoodLog): Resource<Unit> = try {
        dao.delete(FoodLogLocalMapper.toEntity(log))
        FirestorePaths.foodLog(log.userId, log.id).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete food log", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.foodLogs(uid).get().await()
        val logs = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { FoodLogMapper.fromMap(doc.id, uid, it) }
        }
        logs.forEach { dao.upsert(FoodLogLocalMapper.toEntity(it)) }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync food logs", e)
    }
}