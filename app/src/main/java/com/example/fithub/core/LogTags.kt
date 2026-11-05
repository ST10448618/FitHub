package com.example.fithub.core

/**
 * Centralised Logcat tags. Every `Log.*` call in the app must use one of these
 * so `adb logcat -s FitHub/Sync FitHub/Biometric ...` can be filtered reliably.
 *
 * Reference: https://developer.android.com/studio/debug/logcat (accessed February 2026)
 */
object LogTags {
    const val APP: String       = "FitHub/App"
    const val AUTH: String      = "FitHub/Auth"
    const val BIOMETRIC: String = "FitHub/Biometric"
    const val SYNC: String      = "FitHub/Sync"
    const val PUSH: String      = "FitHub/Push"
    const val LOCALE: String    = "FitHub/Locale"
    const val STORAGE: String   = "FitHub/Storage"
    const val NET: String       = "FitHub/Net"
    const val NAV: String       = "FitHub/Nav"
    const val STREAK: String    = "FitHub/Streak"
    const val NETWORK = "FitHub/Network"
}