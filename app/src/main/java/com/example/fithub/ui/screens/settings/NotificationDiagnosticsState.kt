package com.example.fithub.ui.screens.settings

/**
 * Diagnostics data shown at the bottom of the Settings screen
 *
 * [tokenPreview] is a truncated, non-sensitive prefix so the row is useful
 * during a demo without showing the full token in a screenshot. Tapping the
 * row copies the full token to the clipboard.
 */
data class NotificationDiagnosticsState(
    val tokenPreview: String? = null,
    val tokenCopied: Boolean = false,
    val permissionGranted: Boolean = false,
    val permissionMessage: String? = null
)