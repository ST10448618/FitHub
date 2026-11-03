/**
 * Shared vocabulary. These lists match the Kotlin enums in the Android app
 * (domain/model/Enums.kt) so values travel unchanged between app and API.
 * Plain arrays + union types are used instead of TypeScript "enum" so Node can run the files directly.
 */
export const GENDERS = ['MALE', 'FEMALE', 'OTHER'] as const;
export type Gender = (typeof GENDERS)[number];

export const ACTIVITY_LEVELS = ['SEDENTARY', 'LIGHTLY_ACTIVE', 'MODERATELY_ACTIVE', 'VERY_ACTIVE', 'EXTREMELY_ACTIVE'] as const;
export type ActivityLevel = (typeof ACTIVITY_LEVELS)[number];

export const GOAL_DIRECTIONS = ['LOSE', 'MAINTAIN', 'GAIN'] as const;
export type GoalDirection = (typeof GOAL_DIRECTIONS)[number];

export const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'] as const;
export type MealType = (typeof MEAL_TYPES)[number];

export const FOOD_SOURCES = ['BARCODE', 'SEARCH', 'AI_RECOGNITION', 'MANUAL'] as const;
export type FoodSource = (typeof FOOD_SOURCES)[number];

export const SESSION_STATUSES = ['COMPLETED', 'PARTIAL', 'DISCARDED'] as const;
export type SessionStatus = (typeof SESSION_STATUSES)[number];

export const WORKOUT_CATEGORIES = ['GENERAL_FITNESS', 'CARDIO', 'STRENGTH', 'ENDURANCE', 'MOBILITY', 'CREATED'] as const;
export const WORKOUT_DIFFICULTIES = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'] as const;
export const EXERCISE_CATEGORIES = ['CHEST', 'BACK', 'LEGS', 'ARMS', 'CORE', 'CARDIO', 'MOBILITY', 'GYM'] as const;

export interface MealTargets {
  breakfastKcal: number;
  lunchKcal: number;
  dinnerKcal: number;
  snackKcal: number;
}

export interface MacroTargets {
  proteinG: number;
  carbsG: number;
  fatG: number;
}
