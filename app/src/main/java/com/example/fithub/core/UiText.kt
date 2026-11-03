package com.example.fithub.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Reference: https://developer.android.com/reference/androidx/annotation/StringRes
 * (accessed February 2026)
 */
sealed interface UiText {

    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class Dynamic(val value: String) : UiText

    @Composable
    fun asString(): String = when (this) {
        is StringResource -> if (args.isEmpty()) {
            stringResource(resId)
        } else {
            stringResource(resId, *args.toTypedArray())
        }
        is Dynamic -> value
    }

    fun asString(context: Context): String = when (this) {
        is StringResource -> if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }
        is Dynamic -> value
    }
}