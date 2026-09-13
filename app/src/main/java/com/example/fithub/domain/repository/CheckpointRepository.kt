package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.CheckpointSchedule
import kotlinx.coroutines.flow.Flow

interface CheckpointRepository {
    fun observe(uid: String): Flow<CheckpointSchedule?>
    suspend fun get(uid: String): CheckpointSchedule?
    suspend fun save(schedule: CheckpointSchedule): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}