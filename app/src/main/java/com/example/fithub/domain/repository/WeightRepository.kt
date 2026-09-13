package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeAll(uid: String): Flow<List<WeightEntry>>
    fun observeLatest(uid: String): Flow<WeightEntry?>
    suspend fun getLatest(uid: String): WeightEntry?
    suspend fun addEntry(entry: WeightEntry): Resource<Unit>
    suspend fun deleteEntry(entryId: String): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}