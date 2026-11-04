// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.ExerciseCategory
import com.example.fithub.domain.model.Gender
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.domain.model.MealType
import com.example.fithub.domain.model.SessionStatus
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutDifficulty

/**
 * Composable helpers for enum labels. Every screen that surfaces enum text
 * uses `enum.localized()` so a language switch re-renders labels without
 * restarting the app.
 *
 * Reference: https://developer.android.com/guide/topics/resources/localization
 * (accessed February 2026)
 */
@Composable
fun ActivityLevel.localized(): String = stringResource(this.labelRes)

@Composable
fun MealType.localized(): String = stringResource(this.labelRes)

@Composable
fun ExerciseCategory.localized(): String = stringResource(this.labelRes)

@Composable
fun WorkoutCategory.localized(): String = stringResource(this.labelRes)

@Composable
fun WorkoutDifficulty.localized(): String = stringResource(this.labelRes)

@Composable
fun SessionStatus.localized(): String = stringResource(this.labelRes)

@Composable
fun GoalDirection.localized(): String = stringResource(this.labelRes)

@Composable
fun Gender.localized(): String = stringResource(this.labelRes)