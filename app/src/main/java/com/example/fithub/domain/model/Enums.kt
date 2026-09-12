package com.example.fithub.domain.model

enum class Gender { MALE, FEMALE, OTHER }

enum class ActivityLevel(val multiplier: Double, val label: String) {
    SEDENTARY(1.20, "Sedentary Lifestyle"),
    LIGHTLY_ACTIVE(1.375, "Lightly Active"),
    MODERATELY_ACTIVE(1.55, "Moderately Active"),
    VERY_ACTIVE(1.725, "Very Active"),
    EXTREMELY_ACTIVE(1.90, "Extremely Active")
}

enum class GoalDirection { LOSE, MAINTAIN, GAIN }

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

enum class FoodSource { BARCODE, SEARCH, AI_RECOGNITION, MANUAL }

enum class WorkoutDifficulty { BEGINNER, INTERMEDIATE, ADVANCED }

enum class WorkoutCategory { GENERAL_FITNESS, CARDIO, STRENGTH, ENDURANCE, MOBILITY, CREATED }

enum class ExerciseCategory { CHEST, BACK, LEGS, ARMS, CORE, CARDIO, MOBILITY, GYM }

enum class SessionStatus { COMPLETED, PARTIAL, DISCARDED }

enum class AchievementCategory { NUTRITION, WORKOUT }