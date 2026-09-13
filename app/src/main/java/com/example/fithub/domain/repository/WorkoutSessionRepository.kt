package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutSessionRepository {
    fun observeCompleted(uid: String): Flow<List<WorkoutSession>>
    fun observeByDate(uid: String, date: LocalDate): Flow<List<WorkoutSession>>
    fun observeRecent(uid: String, limit: Int = 20): Flow<List<WorkoutSession>>
    suspend fun getBetween(uid: String, from: LocalDate, to: LocalDate): List<WorkoutSession>
    suspend fun countBetween(uid: String, from: LocalDate, to: LocalDate): Int
    suspend fun save(session: WorkoutSession): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}