package com.example.fithub

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.core.i18n.LocaleCodes
import com.example.fithub.core.i18n.LocaleManager
import com.example.fithub.core.notifications.NotificationHelper
import com.example.fithub.core.sync.SyncWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FitHubApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Phase 5: apply the user's chosen language to the application context so
     * that [getString] inside services and workers (notifications, channel
     * names, reminder text) uses the correct locale. Runs before `onCreate`,
     * which is essential — `NotificationHelper.createChannels` in `onCreate`
     * reads channel names from the resources.
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(LogTags.APP, "Application.onCreate — process start")

        FirebaseApp.initializeApp(this)
        ServiceLocator.init(this)

        // Phase 5: seed the sync mirror for the language if this is the first
        // launch after upgrade. Guarded internally; runs at most once.
        LocaleManager.migrateOnFirstRun(this) {
            ServiceLocator.preferencesManager.language.first()
        }
        Log.i(LogTags.LOCALE, "Current app locale = ${LocaleManager.currentCode(this)}")

        NotificationHelper.createChannels(this)

        val lockEnabled = ServiceLocator.preferencesManager.isBiometricLockEnabledSync()
        val existingUid = FirebaseAuth.getInstance().currentUser?.uid
        if (lockEnabled && existingUid != null) {
            Log.i(LogTags.AUTH, "Cold start with biometric lock enabled — keeping session")
        } else {
            Log.i(LogTags.AUTH, "Cold start with lockEnabled=$lockEnabled — signing out")
            FirebaseAuth.getInstance().signOut()
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        appScope.launch {
            ServiceLocator.seedManager.seedIfNeeded()
            ServiceLocator.seedManager.seedFoodsIfNeeded()

            startPhase3SyncTriggers()

            // Phase 4: topics (language code now uses LocaleCodes, not the raw
            // DataStore value — keeps topics aligned with the current locale).
            if (ServiceLocator.preferencesManager.pushNotificationsEnabled.first()) {
                val langCode = LocaleCodes.normalise(
                    LocaleManager.currentCode(this@FitHubApplication)
                )
                FirebaseMessaging.getInstance().subscribeToTopic("fithub_all")
                FirebaseMessaging.getInstance().subscribeToTopic("lang_$langCode")
                Log.i(LogTags.PUSH, "Cold start subscribed to topics fithub_all, lang_$langCode")
            }
        }
    }

    private fun startPhase3SyncTriggers() {
        val uid = SessionManager.currentUserId
        if (uid != null) {
            SyncWorker.enqueue(this)
            appScope.launch {
                runCatching { ServiceLocator.syncManager.flush(uid) }
                    .onFailure { Log.e(LogTags.SYNC, "Cold-start flush threw", it) }
            }
        }

        appScope.launch {
            var first = true
            var lastOnline: Boolean? = null
            ServiceLocator.connectivityMonitor.isOnline.collectLatest { online ->
                val previous = lastOnline
                lastOnline = online
                if (first) { first = false; return@collectLatest }
                if (previous != online && online) {
                    val currentUid = SessionManager.currentUserId ?: return@collectLatest
                    runCatching { ServiceLocator.syncManager.flush(currentUid) }
                        .onFailure { Log.e(LogTags.SYNC, "Connectivity flush threw", it) }
                }
            }
        }
    }
}