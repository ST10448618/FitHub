package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.UserProfileDao
import com.example.fithub.data.local.dao.WeightEntryDao
import com.example.fithub.data.local.mapper.WeightLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.WeightMapper
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepositoryImpl(
    private val dao: WeightEntryDao,
    private val userDao: UserProfileDao
) : WeightRepository {

    override fun observeAll(uid: String): Flow<List<WeightEntry>> =
        dao.observeAll(uid).map { list ->
            list.map(WeightLocalMapper::toDomain)
        }

    override fun observeLatest(uid: String): Flow<WeightEntry?> =
        dao.observeLatest(uid).map { it?.let(WeightLocalMapper::toDomain) }

    override suspend fun getLatest(uid: String): WeightEntry? =
        dao.getLatest(uid)?.let(WeightLocalMapper::toDomain)

    override suspend fun addEntry(entry: WeightEntry): Resource<Unit> = try {
        // 1. Local write
        dao.upsert(WeightLocalMapper.toEntity(entry))

        // 2. Update profile's currentWeightKg locally
        userDao.updateCurrentWeight(
            entry.userId,
            entry.weightKg,
            java.time.LocalDateTime.now().toString()
        )

        // 3. Push both to Firestore
        FirestorePaths.weightEntry(entry.userId, entry.id)
            .set(WeightMapper.toMap(entry))
            .await()

        FirestorePaths.user(entry.userId)
            .update("currentWeightKg", entry.weightKg)
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save weight entry", e)
    }

    override suspend fun deleteEntry(entryId: String): Resource<Unit> = try {
        val entity = dao.getById(entryId)
            ?: return Resource.Error("Weight entry not found")
        dao.delete(entryId)
        FirestorePaths.weightEntry(entity.userId, entryId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete weight entry", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.weightEntries(uid).get().await()
        val entries = snapshot.documents.mapNotNull { doc ->
            val map = doc.data ?: return@mapNotNull null
            WeightMapper.fromMap(doc.id, uid, map)
        }
        dao.upsertAll(entries.map(WeightLocalMapper::toEntity))
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync weight entries", e)
    }
}