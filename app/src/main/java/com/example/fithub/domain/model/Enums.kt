package com.example.fithub.domain.model

import androidx.annotation.StringRes
import com.example.fithub.R

/**
 * Domain enums. Each user-visible enum now carries a `@StringRes labelRes`
 * (Phase 5) so screens can render it localised via
 * `stringResource(enum.labelRes)` without a brittle when-map. The legacy
 * `ActivityLevel.label: String` field is retained for backward compatibility
 * — all existing callers keep working. New UI should use `labelRes` (or the
 * `ActivityLevel.localized()` helper in `ui/util/EnumLabels.kt`).
 */
enum class Gender(@param:StringRes val labelRes: Int) {
    MALE(R.string.enum_gender_male),
    FEMALE(R.string.enum_gender_female),
    OTHER(R.string.enum_gender_other)
}

enum class ActivityLevel(
    val multiplier: Double,
    /** Legacy English label — kept for backward compatibility. */
    val label: String,
    @param:StringRes val labelRes: Int
) {
    SEDENTARY(1.20, "Sedentary Lifestyle", R.string.enum_activity_sedentary),
    LIGHTLY_ACTIVE(1.375, "Lightly Active", R.string.enum_activity_lightly),
    MODERATELY_ACTIVE(1.55, "Moderately Active", R.string.enum_activity_moderately),
    VERY_ACTIVE(1.725, "Very Active", R.string.enum_activity_very),
    EXTREMELY_ACTIVE(1.90, "Extremely Active", R.string.enum_activity_extremely)
}

enum class GoalDirection(@param:StringRes val labelRes: Int) {
    LOSE(R.string.enum_goal_lose),
    MAINTAIN(R.string.enum_goal_maintain),
    GAIN(R.string.enum_goal_gain)
}

enum class MealType(@param:StringRes val labelRes: Int) {
    BREAKFAST(R.string.enum_meal_breakfast),
    LUNCH(R.string.enum_meal_lunch),
    DINNER(R.string.enum_meal_dinner),
    SNACK(R.string.enum_meal_snack)
}

enum class FoodSource(@param:StringRes val labelRes: Int) {
    BARCODE(R.string.enum_source_barcode),
    SEARCH(R.string.enum_source_search),
    AI_RECOGNITION(R.string.enum_source_ai),
    MANUAL(R.string.enum_source_manual)
}

enum class WorkoutDifficulty(@param:StringRes val labelRes: Int) {
    BEGINNER(R.string.enum_difficulty_beginner),
    INTERMEDIATE(R.string.enum_difficulty_intermediate),
    ADVANCED(R.string.enum_difficulty_advanced)
}

enum class WorkoutCategory(@param:StringRes val labelRes: Int) {
    GENERAL_FITNESS(R.string.enum_category_general),
    CARDIO(R.string.enum_category_cardio),
    STRENGTH(R.string.enum_category_strength),
    ENDURANCE(R.string.enum_category_endurance),
    MOBILITY(R.string.enum_category_mobility),
    CREATED(R.string.enum_category_created)
}

enum class ExerciseCategory(@param:StringRes val labelRes: Int) {
    CHEST(R.string.enum_exercise_chest),
    BACK(R.string.enum_exercise_back),
    LEGS(R.string.enum_exercise_legs),
    ARMS(R.string.enum_exercise_arms),
    CORE(R.string.enum_exercise_core),
    CARDIO(R.string.enum_exercise_cardio),
    MOBILITY(R.string.enum_exercise_mobility),
    GYM(R.string.enum_exercise_gym)
}

enum class SessionStatus(@param:StringRes val labelRes: Int) {
    COMPLETED(R.string.enum_session_completed),
    PARTIAL(R.string.enum_session_partial),
    DISCARDED(R.string.enum_session_discarded)
}

enum class AchievementCategory(@param:StringRes val labelRes: Int) {
    NUTRITION(R.string.enum_ach_cat_nutrition),
    WORKOUT(R.string.enum_ach_cat_workout)
}