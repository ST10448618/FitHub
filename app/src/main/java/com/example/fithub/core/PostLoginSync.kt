// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reference: https://firebase.google.com/docs/firestore/query-data/get-data#get_all_documents
 * (accessed February 2026)
 */
object PostLoginSync {

    /**
     * Pulls Firestore state into Room for [uid]. Safe to call from any coroutine
     * dispatcher; runs its work on [Dispatchers.IO]. Individual repository
     * failures are logged but do not abort the remaining syncs.
     */
    suspend fun run(uid: String) = withContext(Dispatchers.IO) {
        Log.i(LogTags.SYNC, "PostLoginSync start uid=${uid.take(8)}…")
        runCatching { ServiceLocator.userRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "user sync failed", it) }
        runCatching { ServiceLocator.weightRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "weight sync failed", it) }
        runCatching { ServiceLocator.goalRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "goal sync failed", it) }
        runCatching { ServiceLocator.nutritionGoalsRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "nutrition goals sync failed", it) }
        runCatching { ServiceLocator.workoutGoalsRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "workout goals sync failed", it) }
        runCatching { ServiceLocator.checkpointRepository.syncFromRemote(uid) }
            .onFailure { Log.e(LogTags.SYNC, "checkpoint sync failed", it) }
        Log.i(LogTags.SYNC, "PostLoginSync finished uid=${uid.take(8)}…")
    }
}