// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.MealType

/**
 * Composable helpers for enum labels. Expanded as batches B/C/D/E extract
 * more screens.
 *
 * Reference: https://developer.android.com/guide/topics/resources/localization
 * (accessed February 2026)
 */
@Composable
fun ActivityLevel.localized(): String = stringResource(this.labelRes)

@Composable
fun MealType.localized(): String = stringResource(this.labelRes)