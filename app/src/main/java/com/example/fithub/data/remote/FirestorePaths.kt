package com.example.fithub.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Central place defining every Firestore path.
 * Never build path strings elsewhere.
 */
object FirestorePaths {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    // Root collections
    val verifiedWorkoutPlans: CollectionReference
        get() = db.collection("verified_workout_plans")

    fun verifiedWorkoutPlan(planId: String): DocumentReference =
        verifiedWorkoutPlans.document(planId)

    // Per-user document
    fun user(uid: String): DocumentReference = db.collection("users").document(uid)

    // Per-user subcollections
    fun weightEntries(uid: String): CollectionReference =
        user(uid).collection("weight_entries")

    fun weightEntry(uid: String, id: String): DocumentReference =
        weightEntries(uid).document(id)

    fun goals(uid: String): CollectionReference = user(uid).collection("goals")
    fun goal(uid: String, id: String): DocumentReference = goals(uid).document(id)

    fun nutritionGoals(uid: String): CollectionReference =
        user(uid).collection("nutrition_goals")

    fun nutritionGoal(uid: String, id: String): DocumentReference =
        nutritionGoals(uid).document(id)

    fun workoutGoals(uid: String): CollectionReference =
        user(uid).collection("workout_goals")

    fun workoutGoal(uid: String, id: String): DocumentReference =
        workoutGoals(uid).document(id)

    fun checkpointSchedules(uid: String): CollectionReference =
        user(uid).collection("checkpoint_schedules")

    fun checkpointSchedule(uid: String, id: String): DocumentReference =
        checkpointSchedules(uid).document(id)

    fun foodLogs(uid: String): CollectionReference = user(uid).collection("food_logs")
    fun foodLog(uid: String, id: String): DocumentReference = foodLogs(uid).document(id)

    fun workoutSessions(uid: String): CollectionReference =
        user(uid).collection("workout_sessions")

    fun workoutSession(uid: String, id: String): DocumentReference =
        workoutSessions(uid).document(id)

    fun completedExercises(uid: String, sessionId: String): CollectionReference =
        workoutSession(uid, sessionId).collection("completed_exercises")

    fun createdPlans(uid: String): CollectionReference =
        user(uid).collection("created_plans")

    fun createdPlan(uid: String, planId: String): DocumentReference =
        createdPlans(uid).document(planId)

    fun savedPlans(uid: String): CollectionReference = user(uid).collection("saved_plans")
    fun savedPlan(uid: String, planId: String): DocumentReference =
        savedPlans(uid).document(planId)

    fun userAchievements(uid: String): CollectionReference =
        user(uid).collection("user_achievements")

    fun userAchievement(uid: String, achievementId: String): DocumentReference =
        userAchievements(uid).document(achievementId)

    fun particleBalance(uid: String): DocumentReference =
        user(uid).collection("wallet").document("balance")
}