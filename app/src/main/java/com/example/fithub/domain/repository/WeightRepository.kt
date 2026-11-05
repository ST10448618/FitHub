// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
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

    /**
     * Phase 6 (UDF 4): store the Firebase Storage download URL for the photo
     * attached to [entryId]. If [photoUrl] is null, the column is cleared.
     */
    suspend fun updatePhotoUrl(entryId: String, photoUrl: String?): Resource<Unit>
}