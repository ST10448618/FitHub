package com.example.fithub.core.i18n

/**
 * Reference: https://developer.android.com/guide/topics/resources/localization
 *            https://tools.ietf.org/html/bcp47
 * (accessed February 2026)
 */
object LocaleCodes {

    /**
     * Supported language entry.
     * @param code ISO-639-1 language code, e.g. "en", "zu", "af".
     * @param englishName shown in the *system* language picker (Android 13+).
     * @param nativeName shown in the *in-app* picker — always in the language
     *   itself so users recognise it regardless of the current UI language.
     */
    data class SupportedLocale(
        val code: String,
        val englishName: String,
        val nativeName: String
    )

    /** Ordered list — index 0 is the default fallback. */
    val SUPPORTED: List<SupportedLocale> = listOf(
        SupportedLocale(code = "en", englishName = "English", nativeName = "English"),
        SupportedLocale(code = "zu", englishName = "isiZulu", nativeName = "isiZulu"),
        SupportedLocale(code = "af", englishName = "Afrikaans", nativeName = "Afrikaans")
    )

    /** Fallback ISO code. Never empty. */
    const val DEFAULT: String = "en"

    /** True when [code] is exactly one of the supported ISO codes. */
    fun isSupported(code: String?): Boolean =
        code != null && SUPPORTED.any { it.code == code }

    /**
     * Normalises anything the system or DataStore might hand us into one of
     * the supported codes.
     *  • legacy `"ENGLISH"`, `"ZULU"`, `"AFRIKAANS"` (case-insensitive) → en/zu/af
     *  • 3-letter ISO 639-2 codes → 2-letter forms
     *  • region suffixes (`zu-ZA`) → base language
     *  • unknown/blank → [DEFAULT].
     */
    fun normalise(raw: String?): String {
        if (raw.isNullOrBlank()) return DEFAULT
        val trimmed = raw.trim()
        val lower = trimmed.lowercase()

        // Legacy display-name values from the prototype DataStore.
        val fromLegacy = when {
            lower == "english" || lower == "en" -> "en"
            lower == "zulu" || lower == "isizulu" || lower == "zu" || lower == "zul" -> "zu"
            lower == "afrikaans" || lower == "af" || lower == "afr" -> "af"
            else -> null
        }
        if (fromLegacy != null) return fromLegacy

        // Take the language part of a BCP-47 tag, e.g. `zu-ZA` → `zu`.
        val base = lower.substringBefore('-').substringBefore('_')
        return if (SUPPORTED.any { it.code == base }) base else DEFAULT
    }

    /** Native name for [code] (returns [DEFAULT]'s native name for unknown). */
    fun nativeName(code: String): String =
        SUPPORTED.firstOrNull { it.code == normalise(code) }?.nativeName
            ?: SUPPORTED.first { it.code == DEFAULT }.nativeName

    /** English name for [code] — used by the Android 13+ system picker label. */
    fun englishName(code: String): String =
        SUPPORTED.firstOrNull { it.code == normalise(code) }?.englishName
            ?: SUPPORTED.first { it.code == DEFAULT }.englishName
}