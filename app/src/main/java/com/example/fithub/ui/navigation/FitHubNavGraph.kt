package com.example.fithub.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

@Composable
fun FitHubNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.SPLASH
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route


    MainScaffold(navController = navController, currentRoute = currentRoute) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // ========================
            // AUTH & ONBOARDING
            // ========================
            composable(Screen.SPLASH) {
                com.example.fithub.ui.screens.splash.SplashScreen(
                    onFinished = {
                        navController.navigate(Screen.LANDING) {
                            popUpTo(Screen.SPLASH) { inclusive = true }
                        }
                    }
                )
            }


            composable(Screen.LANDING) {
                com.example.fithub.ui.screens.landing.LandingScreen(
                    onLoginClick = { navController.navigate(Screen.LOGIN) },
                    onRegisterClick = { navController.navigate(Screen.ONBOARDING_USER_DETAILS) }
                )
            }

            composable(Screen.LOGIN) {
                com.example.fithub.ui.screens.login.LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.DASHBOARD) {
                            popUpTo(Screen.LANDING) { inclusive = true }
                        }
                    },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Screen.ONBOARDING_USER_DETAILS) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep1UserDetails(
                    onCancel = { navController.navigateUp() },
                    onNext = { navController.navigate(Screen.ONBOARDING_BIOMETRIC) }
                )
            }

            composable(Screen.ONBOARDING_BIOMETRIC) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep2Biometric(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_AGE) }
                )
            }

            composable(Screen.ONBOARDING_AGE) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep3Age(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_GENDER) }
                )
            }

            composable(Screen.ONBOARDING_GENDER) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep4Gender(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_HEIGHT) }
                )
            }

            composable(Screen.ONBOARDING_HEIGHT) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep5Height(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_WEIGHT) }
                )
            }

            composable(Screen.ONBOARDING_WEIGHT) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep6Weight(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_LIFESTYLE) }
                )
            }

            composable(Screen.ONBOARDING_LIFESTYLE) {
                com.example.fithub.ui.screens.onboarding.OnboardingStep7Lifestyle(
                    onCancel = { navController.popBackStack(Screen.ONBOARDING_USER_DETAILS, false) },
                    onNext = { navController.navigate(Screen.ONBOARDING_COMPLETE) }
                )
            }

            composable(Screen.ONBOARDING_COMPLETE) {
                com.example.fithub.ui.screens.onboarding.OnboardingCompleteScreen(
                    onFinish = {
                        navController.navigate(Screen.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ========================
            // MAIN TABS
            // ========================
            composable(Screen.DASHBOARD) {
                com.example.fithub.ui.screens.dashboard.DashboardScreen(
                    onNavigate = { navController.navigate(it) },
                    onProfileClick = { navController.navigate(Screen.PROFILE) }
                )
            }

            composable(Screen.JOURNAL) {
                com.example.fithub.ui.screens.journal.JournalScreen(
                    onNavigate = { navController.navigate(it) }
                )
            }

            composable(Screen.PLANS) {
                PlaceholderScreen(
                    title = "Fitness Hub",
                    subtitle = "Recent · Created · Saved · Verified"
                )
            }

            composable(Screen.GOALS) {
                com.example.fithub.ui.screens.goals.GoalsTargetsScreen(
                    onNavigate = { navController.navigate(it) }
                )
            }

            composable(Screen.REWARDS) {
                PlaceholderScreen(
                    title = "Rewards",
                    subtitle = "Particles · Achievements"
                )
            }

            // ========================
            // PROGRESS
            // ========================
            composable(Screen.PROGRESS_OVERVIEW) {
                PlaceholderScreen(
                    title = "Progress Overview",
                    onBack = { navController.navigateUp() },
                    subtitle = "Weight · Nutrition · Workouts"
                )
            }
            composable(Screen.NUTRITION_OVERVIEW) {
                PlaceholderScreen(
                    title = "Nutrition Overview",
                    onBack = { navController.navigateUp() },
                    subtitle = "Goals vs Actual"
                )
            }
            composable(Screen.WORKOUTS_OVERVIEW) {
                PlaceholderScreen(
                    title = "Workouts Overview",
                    onBack = { navController.navigateUp() },
                    subtitle = "Activity · Habits · Breakdown"
                )
            }

            // ========================
            // BODY & WEIGHT
            // ========================
            composable(Screen.BODY_WEIGHT) {
                com.example.fithub.ui.screens.body.BodyWeightScreen(
                    onBack = { navController.navigateUp() },
                    onLogWeightClick = { navController.navigate(Screen.CHECKPOINT_LOGGER) },
                    onManageCheckpointsClick = { navController.navigate(Screen.CHECKPOINT_MANAGER) }
                )
            }

            composable(Screen.CHECKPOINT_LOGGER) {
                com.example.fithub.ui.screens.body.CheckpointLoggerScreen(
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }
            composable(Screen.CHECKPOINT_MANAGER) {
                com.example.fithub.ui.screens.goals.checkpoint.CheckpointManagerScreen(
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }
            composable(Screen.WEIGHT_GOAL) {
                com.example.fithub.ui.screens.goals.weight.WeightGoalScreen(
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }

            // ========================
            // FOOD
            // ========================
            composable(Screen.ADD_MEAL) {
                com.example.fithub.ui.screens.food.addmeal.AddMealScreen(
                    onBack = { navController.navigateUp() },
                    onFoodSelected = { foodId ->
                        navController.navigate(Screen.foodDetails(foodId))
                    },
                    onBarcodeScan = { navController.navigate(Screen.BARCODE_SCANNER) }
                )
            }

            composable(Screen.LIST_FOOD) {
                com.example.fithub.ui.screens.food.list.FoodListScreen(
                    onBack = { navController.navigateUp() },
                    onFoodSelected = { foodId ->
                        navController.navigate(Screen.foodDetails(foodId))
                    }
                )
            }





            // Simple food details (used from Add Meal / Food List)
            composable(
                route = Screen.FOOD_DETAILS,
                arguments = listOf(navArgument("foodId") { type = NavType.StringType })
            ) { entry ->
                com.example.fithub.ui.screens.food.details.FoodDetailsScreen(
                    onBack = { navController.navigateUp() },
                    onAdded = {
                        navController.popBackStack(Screen.ADD_MEAL, inclusive = false)
                    }
                )
            }

            // Contextual food details (used from Journal, with qty + meal prefilled)
            composable(
                route = Screen.FOOD_DETAILS_WITH_CONTEXT,
                arguments = listOf(
                    navArgument("foodId") { type = NavType.StringType },
                    navArgument("qty") { type = NavType.StringType; defaultValue = "1" },
                    navArgument("meal") { type = NavType.StringType; defaultValue = "BREAKFAST" }
                )
            ) { entry ->
                com.example.fithub.ui.screens.food.details.FoodDetailsScreen(
                    onBack = { navController.navigateUp() },
                    onAdded = {
                        // After adding, close details and stay on Journal
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.LIST_FOOD_LOGS,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { entry ->
                com.example.fithub.ui.screens.food.list.FoodLogListScreen(
                    onBack = { navController.navigateUp() },
                    onLogClick = { log ->
                        if (log.foodId != null) {
                            navController.navigate(
                                Screen.foodDetailsWithContext(
                                    foodId = log.foodId,
                                    quantity = log.portionSize.toInt().coerceAtLeast(1),
                                    mealType = log.mealType.name
                                )
                            )
                        }
                    }
                )
            }



            // ========================
            // WORKOUTS
            // ========================
            composable(Screen.VERIFIED_PLANS) {
                PlaceholderScreen(
                    title = "Verified Workout Plans",
                    onBack = { navController.navigateUp() },
                    subtitle = "Categories · Search · Filter"
                )
            }
            composable(
                route = Screen.LIST_WORKOUTS,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { entry ->
                val category = entry.arguments?.getString("category").orEmpty()
                PlaceholderScreen(
                    title = "List · $category",
                    onBack = { navController.navigateUp() },
                    subtitle = "Browse workouts"
                )
            }
            composable(
                route = Screen.WORKOUT_DETAILS,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { entry ->
                val planId = entry.arguments?.getString("planId").orEmpty()
                PlaceholderScreen(
                    title = "Workout Plan Details",
                    onBack = { navController.navigateUp() },
                    subtitle = "planId: $planId"
                )
            }

            composable(Screen.CREATE_WORKOUT_1) {
                PlaceholderScreen(
                    title = "Create Workout · Step 1",
                    onBack = { navController.navigateUp() },
                    subtitle = "Name & details"
                )
            }
            composable(Screen.CREATE_WORKOUT_2) {
                PlaceholderScreen(
                    title = "Create Workout · Step 2",
                    onBack = { navController.navigateUp() },
                    subtitle = "Exercise selection"
                )
            }
            composable(Screen.CREATE_WORKOUT_3) {
                PlaceholderScreen(
                    title = "Create Workout · Step 3",
                    onBack = { navController.navigateUp() },
                    subtitle = "Configure exercises"
                )
            }
            composable(Screen.CREATE_WORKOUT_4) {
                PlaceholderScreen(
                    title = "Create Workout · Step 4",
                    onBack = { navController.navigateUp() },
                    subtitle = "Review & create"
                )
            }

            composable(
                route = Screen.SESSION_MODE,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { entry ->
                val planId = entry.arguments?.getString("planId").orEmpty()
                PlaceholderScreen(
                    title = "Session Mode",
                    subtitle = "planId: $planId"
                )
            }
            composable(
                route = Screen.SESSION_COMPLETED,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { entry ->
                val sessionId = entry.arguments?.getString("sessionId").orEmpty()
                PlaceholderScreen(
                    title = "Session Completed",
                    subtitle = "sessionId: $sessionId"
                )
            }

            // ========================
            // GOALS
            // ========================
            composable(Screen.NUTRITION_GOALS) {
                PlaceholderScreen(
                    title = "Nutrition Goals",
                    onBack = { navController.navigateUp() },
                    subtitle = "Calories · Meals · Macros"
                )
            }
            composable(Screen.WORKOUT_GOALS) {
                com.example.fithub.ui.screens.goals.workout.WorkoutGoalsScreen(
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }

            // ========================
            // PROFILE & SETTINGS
            // ========================
            composable(Screen.PROFILE) {
                com.example.fithub.ui.screens.profile.ProfileScreen(
                    onBack = { navController.navigateUp() },
                    onEditClick = { navController.navigate(Screen.EDIT_PROFILE) },
                    onSettingsClick = { navController.navigate(Screen.SETTINGS) }
                )
            }

            composable(Screen.EDIT_PROFILE) {
                com.example.fithub.ui.screens.profile.EditProfileScreen(
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }

            composable(Screen.SETTINGS) {
                com.example.fithub.ui.screens.settings.SettingsScreen(
                    onBack = { navController.navigateUp() }
                )
            }

            // ========================
            // ACHIEVEMENTS
            // ========================
            composable(Screen.ACHIEVEMENTS) {
                PlaceholderScreen(
                    title = "Achievements",
                    onBack = { navController.navigateUp() },
                    subtitle = "Level · Nutrition · Workout"
                )
            }
        }
    }
}