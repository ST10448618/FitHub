package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.FoodLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FoodLogRepository {
    fun observeByDate(uid: String, date: LocalDate): Flow<List<FoodLog>>
    fun observeRecent(uid: String, limit: Int = 50): Flow<List<FoodLog>>
    suspend fun getByDate(uid: String, date: LocalDate): List<FoodLog>
    suspend fun getBetween(uid: String, from: LocalDate, to: LocalDate): List<FoodLog>
    suspend fun add(log: FoodLog): Resource<Unit>
    suspend fun delete(log: FoodLog): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}