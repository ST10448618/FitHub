package com.example.fithub.data.seed

import com.example.fithub.data.local.FitHubDatabase
import com.example.fithub.data.local.mapper.ExerciseLocalMapper
import com.example.fithub.data.local.mapper.WorkoutPlanLocalMapper
import com.example.fithub.domain.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Responsible for populating the local database with seed data on first launch.
 * Safe to call multiple times — checks if data already exists before inserting.
 */
class SeedManager(
    private val db: FitHubDatabase,
    private val exerciseRepository: ExerciseRepository
) {

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        seedExercisesIfNeeded()
        seedVerifiedPlansIfNeeded()
    }

    suspend fun seedFoodsIfNeeded() = withContext(Dispatchers.IO) {
        val count = db.foodDao().count()   // You'll need to add this to FoodDao
        if (count >= 20) return@withContext
        db.foodDao().upsertAll(FoodSeed.foods())
    }

    private suspend fun seedExercisesIfNeeded() {
        val existing = db.exerciseDao().count()
        if (existing > 0) return

        val entities = SeedData.exercises.map(ExerciseLocalMapper::toEntity)
        db.exerciseDao().upsertAll(entities)
    }

    private suspend fun seedVerifiedPlansIfNeeded() {
        val existing = db.workoutPlanDao().verifiedCount()
        if (existing > 0) return

        val plans = SeedData.verifiedPlans()

        plans.forEach { plan ->
            db.workoutPlanDao().upsert(WorkoutPlanLocalMapper.toEntity(plan))
            db.workoutExerciseDao().deleteForPlan(plan.id)
            db.workoutExerciseDao().upsertAll(
                WorkoutPlanLocalMapper.toExerciseEntities(plan.id, plan.exercises)
            )
        }
    }
}