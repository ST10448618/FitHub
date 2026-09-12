package com.example.fithub.data.local.converter

import androidx.room.TypeConverter
import com.example.fithub.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    // ---------- Date / Time ----------
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    // ---------- Enums ----------
    @TypeConverter
    fun genderToString(v: Gender): String = v.name
    @TypeConverter
    fun stringToGender(v: String): Gender = Gender.valueOf(v)

    @TypeConverter
    fun activityLevelToString(v: ActivityLevel): String = v.name
    @TypeConverter
    fun stringToActivityLevel(v: String): ActivityLevel = ActivityLevel.valueOf(v)

    @TypeConverter
    fun goalDirectionToString(v: GoalDirection): String = v.name
    @TypeConverter
    fun stringToGoalDirection(v: String): GoalDirection = GoalDirection.valueOf(v)

    @TypeConverter
    fun mealTypeToString(v: MealType): String = v.name
    @TypeConverter
    fun stringToMealType(v: String): MealType = MealType.valueOf(v)

    @TypeConverter
    fun foodSourceToString(v: FoodSource): String = v.name
    @TypeConverter
    fun stringToFoodSource(v: String): FoodSource = FoodSource.valueOf(v)

    @TypeConverter
    fun workoutDifficultyToString(v: WorkoutDifficulty): String = v.name
    @TypeConverter
    fun stringToWorkoutDifficulty(v: String): WorkoutDifficulty =
        WorkoutDifficulty.valueOf(v)

    @TypeConverter
    fun workoutCategoryToString(v: WorkoutCategory): String = v.name
    @TypeConverter
    fun stringToWorkoutCategory(v: String): WorkoutCategory =
        WorkoutCategory.valueOf(v)

    @TypeConverter
    fun exerciseCategoryToString(v: ExerciseCategory): String = v.name
    @TypeConverter
    fun stringToExerciseCategory(v: String): ExerciseCategory =
        ExerciseCategory.valueOf(v)

    @TypeConverter
    fun sessionStatusToString(v: SessionStatus): String = v.name
    @TypeConverter
    fun stringToSessionStatus(v: String): SessionStatus =
        SessionStatus.valueOf(v)
}