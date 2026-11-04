package com.example.fithub.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.fithub.R
import com.example.fithub.domain.model.ActivityLevel

/**
 * Composable helpers for enum labels. Phase 5 Batch B only needs
 * [ActivityLevel.localized]; Phase 5 Batch E will expand this file with the
 * remaining enums as their screens are extracted.
 *
 * Reference: https://developer.android.com/guide/topics/resources/localization
 * (accessed February 2026)
 */
@Composable
fun ActivityLevel.localized(): String = stringResource(this.labelRes)