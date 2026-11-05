// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.ui.navigation

object Screen {

    // ---------- Auth & Onboarding ----------
    const val SPLASH = "splash"
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val BIOMETRIC_GATE = "biometric_gate"

    const val ONBOARDING_USER_DETAILS = "onboarding/user_details"
    const val ONBOARDING_BIOMETRIC = "onboarding/biometric"
    const val ONBOARDING_AGE = "onboarding/age"
    const val ONBOARDING_GENDER = "onboarding/gender"
    const val ONBOARDING_HEIGHT = "onboarding/height"
    const val ONBOARDING_WEIGHT = "onboarding/weight"
    const val ONBOARDING_LIFESTYLE = "onboarding/lifestyle"
    const val ONBOARDING_COMPLETE = "onboarding/complete"

    // ---------- Main bottom-nav tabs ----------
    const val DASHBOARD = "dashboard"
    const val JOURNAL = "journal"
    const val PLANS = "plans"
    const val GOALS = "goals"
    const val REWARDS = "RewardsScreen"

    // ---------- Progress / Overview ----------
    const val PROGRESS_OVERVIEW = "progress_overview"
    const val NUTRITION_OVERVIEW = "nutrition_overview"
    const val WORKOUTS_OVERVIEW = "workouts_overview"

    // ---------- Body & Weight ----------
    const val BODY_WEIGHT = "body_weight"
    const val CHECKPOINT_LOGGER = "checkpoint_logger"
    const val CHECKPOINT_MANAGER = "checkpoint_manager"
    const val WEIGHT_GOAL = "weight_goal"

    /** Phase 6 (UDF 4): progress photo gallery. */
    const val PROGRESS_GALLERY = "progress_gallery"

    // ---------- Food ----------
    const val ADD_MEAL = "add_meal"
    const val LIST_FOOD = "list_food/{category}"
    const val FOOD_DETAILS = "food_details/{foodId}"
    const val BARCODE_SCANNER = "barcode_scanner"
    const val LIST_FOOD_LOGS = "list_food_logs/{date}"

    const val FOOD_DETAILS_WITH_CONTEXT =
        "food_details/{foodId}?qty={qty}&meal={meal}"

    fun listFoodLogs(date: String) = "list_food_logs/$date"

    fun foodDetailsWithContext(
        foodId: String,
        quantity: Int = 1,
        mealType: String = "BREAKFAST"
    ) = "food_details/$foodId?qty=$quantity&meal=$mealType"

    // ---------- Workouts ----------
    const val VERIFIED_PLANS = "verified_plans"
    const val LIST_WORKOUTS = "list_workouts/{category}"
    const val WORKOUT_DETAILS = "workout_details/{planId}"

    const val CREATE_WORKOUT_1 = "create_workout/step1"
    const val CREATE_WORKOUT_2 = "create_workout/step2"
    const val CREATE_WORKOUT_3 = "create_workout/step3"
    const val CREATE_WORKOUT_4 = "create_workout/step4"

    const val SESSION_MODE = "session_mode/{planId}"
    const val SESSION_COMPLETED = "session_completed/{sessionId}"

    // ---------- Goals ----------
    const val NUTRITION_GOALS = "nutrition_goals"
    const val WORKOUT_GOALS = "workout_goals"

    // ---------- Profile & Settings ----------
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"

    // ---------- Achievements ----------
    const val ACHIEVEMENTS = "achievements"

    // ---------- Builders ----------
    fun foodDetails(foodId: String) = "food_details/$foodId"
    fun listFood(category: String) = "list_food/$category"
    fun workoutDetails(planId: String) = "workout_details/$planId"
    fun listWorkouts(category: String) = "list_workouts/$category"
    fun sessionMode(planId: String) = "session_mode/$planId"
    fun sessionCompleted(sessionId: String) = "session_completed/$sessionId"

    // ---------- Route groups ----------
    val routesWithBottomBar: Set<String> = setOf(
        LIST_FOOD_LOGS,
        DASHBOARD, JOURNAL, PLANS, GOALS, REWARDS,
        PROGRESS_OVERVIEW, NUTRITION_OVERVIEW, WORKOUTS_OVERVIEW,
        BODY_WEIGHT, PROGRESS_GALLERY,
        ADD_MEAL,
        VERIFIED_PLANS,
        PROFILE, EDIT_PROFILE, SETTINGS,
        ACHIEVEMENTS,
        BARCODE_SCANNER
    )

    val mainTabRoutes: Set<String> = setOf(
        DASHBOARD, JOURNAL, PLANS, GOALS, REWARDS
    )
}