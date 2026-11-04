package com.example.fithub.core.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.example.fithub.core.LogTags
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * References:
 *  • https://developer.android.com/guide/topics/resources/app-languages
 *  • https://developer.android.com/reference/android/content/Context#createConfigurationContext(android.content.res.Configuration)
 * (accessed February 2026)
 */
object LocaleManager {

    private const val PREFS_NAME = "fithub_locale"
    private const val KEY_CURRENT = "current_language"
    private const val KEY_MIGRATED = "locale_migrated_v1"

    // ---- Synchronous mirror ----

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Current language code, guaranteed to be one of
     * [LocaleCodes.SUPPORTED]. Reads only the sync mirror — never blocks on
     * DataStore, safe to call from `attachBaseContext`.
     */
    fun currentCode(context: Context): String {
        val raw = prefs(context).getString(KEY_CURRENT, null)
        return LocaleCodes.normalise(raw)
    }

    /** Persist the code into both stores. Safe to call from any thread. */
    fun persistSync(context: Context, code: String) {
        val normalised = LocaleCodes.normalise(code)
        prefs(context).edit().putString(KEY_CURRENT, normalised).apply()
        Log.i(LogTags.LOCALE, "Locale persisted synchronously: $normalised")
    }

    // ---- Context wrapping ----

    /**
     * Wraps [base] with a [Configuration] whose primary Locale is the current
     * language. Call from `attachBaseContext` in Activities, Workers, Services.
     */
    fun wrap(base: Context): Context {
        val code = currentCode(base)
        val locale = Locale.forLanguageTag(code)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setLocales(LocaleList(locale))
            }
        }
        return base.createConfigurationContext(config)
    }

    /**
     * Applies [code] to the activity's resources and recreates it so the new
     * strings take effect. Persists to both stores first, then logs, then
     * recreates — the order matters: if recreation happened first, the new
     * activity's `attachBaseContext` would read the old mirror.
     */
    fun apply(activity: FragmentActivity, code: String) {
        val normalised = LocaleCodes.normalise(code)
        persistSync(activity, normalised)
        Log.i(LogTags.LOCALE, "applyLocale → $normalised; recreating activity")
        activity.recreate()
    }

    /**
     * Called once from [com.example.fithub.FitHubApplication.onCreate].
     *
     * If the mirror is empty (fresh install or upgrade from a pre-Phase-5
     * build), seed it from:
     *  1. the legacy DataStore `language` value if it's anything other than
     *     the default (`"ENGLISH"` is mapped to `en`, `"ZULU"` → `zu`,
     *     `"AFRIKAANS"` → `af`);
     *  2. otherwise the system Locale if it's one of the supported three;
     *  3. otherwise [LocaleCodes.DEFAULT].
     *
     * The DataStore read is done via a single `runBlocking` — this only runs
     * on the very first cold start after upgrade and is guarded by a sync
     * flag, so we never pay the cost again.
     */
    fun migrateOnFirstRun(context: Context, dataStoreLanguage: suspend () -> String) {
        val p = prefs(context)
        if (p.getBoolean(KEY_MIGRATED, false)) {
            Log.d(LogTags.LOCALE, "migrateOnFirstRun: already migrated; current=${currentCode(context)}")
            return
        }
        val currentMirror = p.getString(KEY_CURRENT, null)
        if (!currentMirror.isNullOrBlank()) {
            // Already seeded (e.g. user toggled before migration guard ran).
            p.edit().putBoolean(KEY_MIGRATED, true).apply()
            Log.d(LogTags.LOCALE, "migrateOnFirstRun: mirror already set to $currentMirror")
            return
        }
        val dataStoreValue = runCatching { runBlocking { dataStoreLanguage() } }
            .getOrElse { "ENGLISH" }
        val systemLocale = Locale.getDefault().language
        val chosen = when {
            !dataStoreValue.isNullOrBlank() && !dataStoreValue.equals("ENGLISH", true) ->
                LocaleCodes.normalise(dataStoreValue)
            LocaleCodes.isSupported(systemLocale) -> systemLocale
            else -> LocaleCodes.DEFAULT
        }
        p.edit()
            .putString(KEY_CURRENT, chosen)
            .putBoolean(KEY_MIGRATED, true)
            .apply()
        Log.i(
            LogTags.LOCALE,
            "migrateOnFirstRun: dataStore='$dataStoreValue' system='$systemLocale' → '$chosen'"
        )
    }
}