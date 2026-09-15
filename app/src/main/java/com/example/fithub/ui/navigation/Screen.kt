package com.example.fithub.ui.navigation

/**
 * All FitHub route constants.
 * Never build route strings elsewhere — always use these.
 */
object Screen {

    // ---------- Auth & Onboarding ----------
    const val SPLASH = "splash"
    const val LANDING = "landing"
    const val LOGIN = "login"

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
    const val REWARDS = "rewards"

    // ---------- Progress / Overview ----------
    const val PROGRESS_OVERVIEW = "progress_overview"
    const val NUTRITION_OVERVIEW = "nutrition_overview"
    const val WORKOUTS_OVERVIEW = "workouts_overview"

    // ---------- Body & Weight ----------
    const val BODY_WEIGHT = "body_weight"
    const val CHECKPOINT_LOGGER = "checkpoint_logger"
    const val CHECKPOINT_MANAGER = "checkpoint_manager"
    const val WEIGHT_GOAL = "weight_goal"

    // ---------- Food ----------
    const val ADD_MEAL = "add_meal"
    const val LIST_FOOD = "list_food/{category}"
    const val FOOD_DETAILS = "food_details/{foodId}"
    const val BARCODE_SCANNER = "barcode_scanner"
    // Route for the list of the day's logged foods
    const val LIST_FOOD_LOGS = "list_food_logs/{date}"

    // Route pattern for food details with pre-filled context
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

    // ---------- Builders (for parameterised routes) ----------
    fun foodDetails(foodId: String) = "food_details/$foodId"
    fun listFood(category: String) = "list_food/$category"
    fun workoutDetails(planId: String) = "workout_details/$planId"
    fun listWorkouts(category: String) = "list_workouts/$category"
    fun sessionMode(planId: String) = "session_mode/$planId"
    fun sessionCompleted(sessionId: String) = "session_completed/$sessionId"

    // ---------- Route groups ----------
    /**
     * Routes that display the permanent bottom navigation.
     * Anything not in here hides the bottom bar (auth, onboarding, modals, sessions).
     */
    val routesWithBottomBar: Set<String> = setOf(
        LIST_FOOD_LOGS,
        DASHBOARD, JOURNAL, PLANS, GOALS, REWARDS,
        PROGRESS_OVERVIEW, NUTRITION_OVERVIEW, WORKOUTS_OVERVIEW,
        BODY_WEIGHT,
        ADD_MEAL,
        VERIFIED_PLANS,
        PROFILE, EDIT_PROFILE, SETTINGS,
        ACHIEVEMENTS

    )

    /** Main bottom-nav tab routes only. */
    val mainTabRoutes: Set<String> = setOf(
        DASHBOARD, JOURNAL, PLANS, GOALS, REWARDS
    )
}