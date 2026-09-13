package com.example.fithub.data.seed

import com.example.fithub.domain.model.*
import java.time.LocalDateTime

/**
 * Authoritative seeded data for the FitHub prototype.
 * All activity values are prototype estimates, not medical values.
 */
object SeedData {

    // ---------------- EXERCISES ----------------

    val exercises: List<Exercise> = listOf(
        // CHEST
        Exercise("ex_pushup", "Push-Ups", ExerciseCategory.CHEST,
            "Classic bodyweight push-up targeting chest, shoulders and triceps.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.5, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_incline_pushup", "Incline Push-Ups", ExerciseCategory.CHEST,
            "Push-ups with hands elevated. Easier variation.",
            WorkoutDifficulty.BEGINNER, "Bench", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_diamond_pushup", "Diamond Push-Ups", ExerciseCategory.CHEST,
            "Hands close together. Emphasises triceps.",
            WorkoutDifficulty.INTERMEDIATE, "None", false,
            estimatedActivityPerRep = 0.7, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),

        // BACK
        Exercise("ex_superman", "Superman", ExerciseCategory.BACK,
            "Lie face down, lift arms and legs together.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_rowing", "Dumbbell Rows", ExerciseCategory.BACK,
            "Bent-over row with dumbbells.",
            WorkoutDifficulty.INTERMEDIATE, "Dumbbells", false,
            estimatedActivityPerRep = 0.7, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),

        // LEGS
        Exercise("ex_squat", "Squats", ExerciseCategory.LEGS,
            "Bodyweight squat targeting quads, glutes, and hamstrings.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.5, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_lunge", "Lunges", ExerciseCategory.LEGS,
            "Alternating forward lunges.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.6, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_deadlift", "Deadlifts", ExerciseCategory.LEGS,
            "Hip-hinge lift with dumbbells or barbell.",
            WorkoutDifficulty.INTERMEDIATE, "Dumbbells", false,
            estimatedActivityPerRep = 0.8, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 4),
        Exercise("ex_stepup", "Step-Ups", ExerciseCategory.LEGS,
            "Step onto a bench or platform, alternating legs.",
            WorkoutDifficulty.BEGINNER, "Bench", false,
            estimatedActivityPerRep = 0.5, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_calfraise", "Calf Raises", ExerciseCategory.LEGS,
            "Rise up onto toes, slow down.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.3, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),
        Exercise("ex_bridge", "Bridge", ExerciseCategory.LEGS,
            "Glute bridge. Lie on back, lift hips.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_burpee", "Burpees", ExerciseCategory.LEGS,
            "Full-body explosive movement: squat, plank, push-up, jump.",
            WorkoutDifficulty.ADVANCED, "None", false,
            estimatedActivityPerRep = 1.2, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 4),

        // ARMS
        Exercise("ex_bicep_curl", "Bicep Curls", ExerciseCategory.ARMS,
            "Curl dumbbells from hips to shoulders.",
            WorkoutDifficulty.BEGINNER, "Dumbbells", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_tricep_dip", "Tricep Dips", ExerciseCategory.ARMS,
            "Dip using a chair or bench.",
            WorkoutDifficulty.INTERMEDIATE, "Bench", false,
            estimatedActivityPerRep = 0.6, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),

        // CORE
        Exercise("ex_plank", "Plank", ExerciseCategory.CORE,
            "Hold a forearm plank. Time-based.",
            WorkoutDifficulty.BEGINNER, "None", true,
            estimatedActivityPerRep = 0.0, estimatedActivityPerSecond = 0.1,
            estimatedSecondsPerRep = 0),
        Exercise("ex_situp", "Sit-Ups", ExerciseCategory.CORE,
            "Classic sit-ups.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),
        Exercise("ex_bicycle_crunch", "Bicycle Crunches", ExerciseCategory.CORE,
            "Alternating elbow-to-opposite-knee crunches.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.5, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),
        Exercise("ex_russian_twist", "Russian Twists", ExerciseCategory.CORE,
            "Seated rotation with weight or bodyweight.",
            WorkoutDifficulty.INTERMEDIATE, "None", false,
            estimatedActivityPerRep = 0.5, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),
        Exercise("ex_mountain_climber", "Mountain Climbers", ExerciseCategory.CORE,
            "Plank position with alternating knee drives.",
            WorkoutDifficulty.INTERMEDIATE, "None", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),

        // CARDIO
        Exercise("ex_jumping_jack", "Jumping Jacks", ExerciseCategory.CARDIO,
            "Jump while raising arms and splitting legs.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.3, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 1),
        Exercise("ex_high_knees", "High Knees", ExerciseCategory.CARDIO,
            "Run in place driving knees high.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.4, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 1),
        Exercise("ex_jump_rope", "Jump Rope", ExerciseCategory.CARDIO,
            "Skipping rope. Time-based.",
            WorkoutDifficulty.BEGINNER, "Jump Rope", true,
            estimatedActivityPerRep = 0.0, estimatedActivityPerSecond = 0.2,
            estimatedSecondsPerRep = 0),
        Exercise("ex_skater", "Skater Jumps", ExerciseCategory.CARDIO,
            "Lateral jump side to side.",
            WorkoutDifficulty.INTERMEDIATE, "None", false,
            estimatedActivityPerRep = 0.6, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),

        // MOBILITY
        Exercise("ex_cat_cow", "Cat-Cow", ExerciseCategory.MOBILITY,
            "Spinal flexion/extension flow.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.2, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 4),
        Exercise("ex_hip_flexor_stretch", "Hip Flexor Stretch", ExerciseCategory.MOBILITY,
            "Kneeling hip flexor stretch. Time-based.",
            WorkoutDifficulty.BEGINNER, "None", true,
            estimatedActivityPerRep = 0.0, estimatedActivityPerSecond = 0.05,
            estimatedSecondsPerRep = 0),
        Exercise("ex_shoulder_roll", "Shoulder Rolls", ExerciseCategory.MOBILITY,
            "Roll shoulders forward/back.",
            WorkoutDifficulty.BEGINNER, "None", false,
            estimatedActivityPerRep = 0.15, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 2),

        // GYM
        Exercise("ex_bench_press", "Bench Press", ExerciseCategory.GYM,
            "Barbell bench press.",
            WorkoutDifficulty.INTERMEDIATE, "Barbell, Bench", false,
            estimatedActivityPerRep = 1.0, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 4),
        Exercise("ex_lat_pulldown", "Lat Pulldown", ExerciseCategory.GYM,
            "Cable lat pulldown.",
            WorkoutDifficulty.INTERMEDIATE, "Cable Machine", false,
            estimatedActivityPerRep = 0.7, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3),
        Exercise("ex_leg_press", "Leg Press", ExerciseCategory.GYM,
            "Machine leg press.",
            WorkoutDifficulty.BEGINNER, "Leg Press Machine", false,
            estimatedActivityPerRep = 0.8, estimatedActivityPerSecond = 0.0,
            estimatedSecondsPerRep = 3)
    )

    // ---------------- VERIFIED WORKOUT PLANS ----------------

    fun verifiedPlans(): List<WorkoutPlan> {
        val now = LocalDateTime.now()

        fun plan(
            id: String, name: String, description: String,
            category: WorkoutCategory, difficulty: WorkoutDifficulty,
            equipment: String, exercises: List<WorkoutExercise>
        ): WorkoutPlan {
            val totalActivity = exercises.sumOf { it.estimatedActivityKcal }.toInt()
            // Rough duration: 40s per rep set + 20s transition per exercise
            val duration = exercises.sumOf { ex ->
                when {
                    ex.targetDurationSeconds > 0 -> ex.targetDurationSeconds / 60 + 1
                    else -> (ex.targetReps * 3) / 60 + 1
                }
            }
            return WorkoutPlan(
                id = id,
                userId = null,
                name = name,
                description = description,
                category = category,
                difficulty = difficulty,
                equipment = equipment,
                imageUrl = null,
                exercises = exercises,
                estimatedActivityKcal = totalActivity,
                estimatedDurationMinutes = duration,
                isVerified = true,
                isSaved = false,
                createdAt = now
            )
        }

        // Helper to build a WorkoutExercise with activity calculated
        fun we(ex: Exercise, reps: Int, idx: Int): WorkoutExercise {
            val activity = if (ex.isTimeBased) {
                // 60 seconds default for time-based exercises
                ex.estimatedActivityPerSecond * 60
            } else {
                ex.estimatedActivityPerRep * reps
            }
            return WorkoutExercise(
                exerciseId = ex.id,
                exerciseName = ex.name,
                category = ex.category,
                orderIndex = idx,
                targetReps = if (ex.isTimeBased) 0 else reps,
                targetDurationSeconds = if (ex.isTimeBased) 60 else 0,
                estimatedActivityKcal = activity
            )
        }

        fun ex(id: String) = exercises.first { it.id == id }

        return listOf(
            plan(
                "plan_home_workout_3b", "Home Workout 3B",
                "A full-body bodyweight routine you can do anywhere.",
                WorkoutCategory.GENERAL_FITNESS, WorkoutDifficulty.BEGINNER, "None",
                listOf(
                    we(ex("ex_pushup"), 12, 0),
                    we(ex("ex_squat"), 15, 1),
                    we(ex("ex_plank"), 0, 2),
                    we(ex("ex_lunge"), 12, 3),
                    we(ex("ex_situp"), 15, 4),
                    we(ex("ex_burpee"), 8, 5)
                )
            ),
            plan(
                "plan_weight_loss_fast", "Weight Loss Fast",
                "High-energy circuit designed for calorie burn.",
                WorkoutCategory.CARDIO, WorkoutDifficulty.INTERMEDIATE, "None",
                listOf(
                    we(ex("ex_jumping_jack"), 30, 0),
                    we(ex("ex_high_knees"), 30, 1),
                    we(ex("ex_burpee"), 10, 2),
                    we(ex("ex_mountain_climber"), 20, 3),
                    we(ex("ex_skater"), 20, 4)
                )
            ),
            plan(
                "plan_sweat_fest", "Sweat Fest",
                "Cardio-focused session to break a sweat.",
                WorkoutCategory.CARDIO, WorkoutDifficulty.INTERMEDIATE, "None",
                listOf(
                    we(ex("ex_jump_rope"), 0, 0),
                    we(ex("ex_jumping_jack"), 40, 1),
                    we(ex("ex_high_knees"), 40, 2),
                    we(ex("ex_burpee"), 12, 3)
                )
            ),
            plan(
                "plan_heart_breaker", "Heart Breaker",
                "Intense cardio with intervals.",
                WorkoutCategory.CARDIO, WorkoutDifficulty.ADVANCED, "None",
                listOf(
                    we(ex("ex_burpee"), 15, 0),
                    we(ex("ex_skater"), 25, 1),
                    we(ex("ex_high_knees"), 50, 2),
                    we(ex("ex_mountain_climber"), 30, 3),
                    we(ex("ex_jumping_jack"), 50, 4)
                )
            ),
            plan(
                "plan_the_popeye", "The Popeye",
                "Arm and upper-body focused.",
                WorkoutCategory.STRENGTH, WorkoutDifficulty.BEGINNER, "Dumbbells",
                listOf(
                    we(ex("ex_bicep_curl"), 12, 0),
                    we(ex("ex_pushup"), 12, 1),
                    we(ex("ex_tricep_dip"), 10, 2),
                    we(ex("ex_rowing"), 12, 3),
                    we(ex("ex_shoulder_roll"), 20, 4)
                )
            ),
            plan(
                "plan_heavy_hitter", "Heavy Hitter",
                "Advanced strength session.",
                WorkoutCategory.STRENGTH, WorkoutDifficulty.ADVANCED, "Dumbbells, Bench",
                listOf(
                    we(ex("ex_deadlift"), 8, 0),
                    we(ex("ex_bench_press"), 8, 1),
                    we(ex("ex_rowing"), 10, 2),
                    we(ex("ex_squat"), 10, 3),
                    we(ex("ex_lunge"), 10, 4)
                )
            ),
            plan(
                "plan_endurance_1a", "Endurance 1A",
                "Build stamina with steady-state cardio.",
                WorkoutCategory.ENDURANCE, WorkoutDifficulty.BEGINNER, "None",
                listOf(
                    we(ex("ex_jumping_jack"), 30, 0),
                    we(ex("ex_jump_rope"), 0, 1),
                    we(ex("ex_high_knees"), 30, 2),
                    we(ex("ex_mountain_climber"), 20, 3)
                )
            ),
            plan(
                "plan_starter_stamina", "Starter Stamina",
                "Beginner endurance build-up.",
                WorkoutCategory.ENDURANCE, WorkoutDifficulty.BEGINNER, "None",
                listOf(
                    we(ex("ex_jumping_jack"), 20, 0),
                    we(ex("ex_high_knees"), 20, 1),
                    we(ex("ex_shoulder_roll"), 15, 2),
                    we(ex("ex_cat_cow"), 8, 3)
                )
            )
        )
    }
}