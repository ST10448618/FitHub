package com.example.fithub.core

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.fithub.R

sealed interface BiometricResult {
    data object Success : BiometricResult
    data class Error(val kind: ErrorKind) : BiometricResult

    enum class ErrorKind {
        CANCELLED,
        LOCKOUT,
        LOCKOUT_PERMANENT,
        NO_BIOMETRICS,
        HARDWARE_UNAVAILABLE,
        UNKNOWN
    }
}

sealed interface BiometricAvailability {
    data object Available : BiometricAvailability
    data object NoneEnrolled : BiometricAvailability
    data object NoHardware : BiometricAvailability
    data object Unavailable : BiometricAvailability
}

object BiometricHelper {

    private val allowedAuthenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): BiometricAvailability {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            else -> BiometricAvailability.Unavailable
        }
    }

    fun isAvailable(context: Context): Boolean =
        canAuthenticate(context) == BiometricAvailability.Available

    /**
     * Shows the system biometric prompt.
     *
     * Note: `@param:` is only valid on primary-constructor parameters, so these
     * three annotations are plain `@StringRes` — no target prefix.
     */
    fun authenticate(
        activity: FragmentActivity,
        @StringRes titleRes: Int = R.string.login_fingerprint_title,
        @StringRes subtitleRes: Int = R.string.login_fingerprint_subtitle,
        @StringRes negativeRes: Int = R.string.generic_cancel,
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.i(LogTags.BIOMETRIC, "Biometric succeeded")
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val kind = mapError(errorCode)
                    Log.w(LogTags.BIOMETRIC, "Biometric error code=$errorCode kind=$kind msg=$errString")
                    onResult(BiometricResult.Error(kind))
                }

                override fun onAuthenticationFailed() {
                    Log.w(LogTags.BIOMETRIC, "Biometric fingerprint mismatch — retrying")
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(titleRes))
            .setSubtitle(activity.getString(subtitleRes))
            .setNegativeButtonText(activity.getString(negativeRes))
            .setAllowedAuthenticators(allowedAuthenticators)
            .setConfirmationRequired(false)
            .build()

        try {
            Log.d(LogTags.BIOMETRIC, "Launching biometric prompt")
            prompt.authenticate(info)
        } catch (t: Throwable) {
            Log.e(LogTags.BIOMETRIC, "Biometric prompt threw", t)
            onResult(BiometricResult.Error(BiometricResult.ErrorKind.UNKNOWN))
        }
    }

    private fun mapError(code: Int): BiometricResult.ErrorKind = when (code) {
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_USER_CANCELED -> BiometricResult.ErrorKind.CANCELLED
        BiometricPrompt.ERROR_LOCKOUT -> BiometricResult.ErrorKind.LOCKOUT
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricResult.ErrorKind.LOCKOUT_PERMANENT
        BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricResult.ErrorKind.NO_BIOMETRICS
        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometricResult.ErrorKind.HARDWARE_UNAVAILABLE
        else -> BiometricResult.ErrorKind.UNKNOWN
    }

    fun openBiometricSettings(activity: FragmentActivity) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    allowedAuthenticators
                )
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        runCatching {
            Log.i(LogTags.BIOMETRIC, "Opening system biometric settings")
            activity.startActivity(intent)
        }.onFailure {
            Log.e(LogTags.BIOMETRIC, "Could not open system biometric settings", it)
        }
    }
}