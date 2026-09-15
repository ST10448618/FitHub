package com.example.fithub.core

import android.content.Context

/**
 * Maps a verified workout plan ID to a local drawable resource name.
 * Returns 0 at lookup time if the drawable doesn't exist yet — the caller
 * then falls back to the gradient placeholder.
 *
 * When you add hero PNGs to `res/drawable-nodpi/`, name them exactly as the
 * `nameFor` mapping below and they will show up automatically — no code change.
 */
object WorkoutImageMapper {

    fun drawableRes(context: Context, planId: String): Int {
        val name = nameFor(planId) ?: return 0
        return context.resources.getIdentifier(
            name, "drawable", context.packageName
        )
    }

    private fun nameFor(planId: String): String? = when (planId) {
        "plan_home_workout_3b" -> "workout_home_3b"
        "plan_weight_loss_fast" -> "workout_weight_loss_fast"
        "plan_sweat_fest" -> "workout_sweat_fest"
        "plan_heart_breaker" -> "workout_heart_breaker"
        "plan_the_popeye" -> "workout_popeye"
        "plan_heavy_hitter" -> "workout_heavy_hitter"
        "plan_endurance_1a" -> "workout_endurance_1a"
        "plan_starter_stamina" -> "workout_starter_stamina"
        else -> null
    }
}