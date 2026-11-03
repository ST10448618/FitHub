package com.example.fithub

import android.app.Application
import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.core.sync.SyncWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FitHubApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.i(LogTags.APP, "Application.onCreate — process start")

        FirebaseApp.initializeApp(this)

        // ============================================================
        // SESSION POLICY (final POE v2)
        //
        // `onCreate()` runs only on cold process start — never on
        // backgrounding, never on rotation. Original prototype behaviour
        // was "always sign out on cold start". That is incompatible with
        // biometric login, because biometric success requires an existing
        // Firebase user whose session the gate can unlock.
        //
        // New policy:
        //   • If biometric lock is ENABLED in preferences AND a Firebase
        //     session still exists  → keep the session; the Splash screen
        //     routes to BiometricGateScreen, which authenticates the
        //     device owner before letting them in.
        //   • Otherwise (lock off, or no session) → sign out exactly as
        //     before.
        //
        // Reference: https://developer.android.com/training/sign-in/biometric-auth
        //            https://firebase.google.com/docs/auth/android/manage-users
        // (accessed February 2026)
        // ============================================================
        val lockEnabled = ServiceLocator.preferencesManager.isBiometricLockEnabledSync()
        val existingUid = FirebaseAuth.getInstance().currentUser?.uid
        if (lockEnabled && existingUid != null) {
            Log.i(
                LogTags.AUTH,
                "Cold start with biometric lock enabled — keeping session for uid=${existingUid.take(8)}…"
            )
        } else {
            Log.i(
                LogTags.AUTH,
                "Cold start with lockEnabled=$lockEnabled, hadSession=${existingUid != null} — signing out"
            )
            FirebaseAuth.getInstance().signOut()
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        ServiceLocator.init(this)

        appScope.launch {
            // 1. Seed verified plans + exercises + foods (needed by both tracks)
            ServiceLocator.seedManager.seedIfNeeded()
            ServiceLocator.seedManager.seedFoodsIfNeeded()

            // 2. Demo user seeding — DISABLED now that real auth is in place.
            //    Uncomment only if you want the Alex Hunter demo profile back.
            // DemoDataSeeder.seedIfNeeded(ServiceLocator.database)

            // 3. Verification logs
            Log.d(
                "FitHubSeed",
                "VERIFIED PLANS: " + ServiceLocator.database.workoutPlanDao().verifiedCount()
            )

            // 4. Phase 3 — cold-start flush: if a user is already signed in
            //    (biometric lock on path, or resumed from a warm process that
            //    was recreated by the OS), drain any pending outbox rows and
            //    ensure a SyncWorker is queued.
            startPhase3SyncTriggers()
        }
    }

    /**
     * Two-way trigger for the sync outbox:
     *  • on cold start (this method, once) — if a session exists
     *  • on every offline → online transition (collected continuously)
     *
     * Never waits for a connectivity event to fire on boot: if the device is
     * already online and a session exists, we flush immediately.
     */
    private fun startPhase3SyncTriggers() {
        // Cold-start kick
        val uid = SessionManager.currentUserId
        if (uid != null) {
            Log.i(LogTags.SYNC, "Cold start with session — enqueue sync worker")
            SyncWorker.enqueue(this)
            appScope.launch {
                runCatching { ServiceLocator.syncManager.flush(uid) }
                    .onFailure { Log.e(LogTags.SYNC, "Cold-start flush threw", it) }
            }
        } else {
            Log.d(LogTags.SYNC, "Cold start without session — skip sync worker")
        }

        // Connectivity-triggered flush. First emission is the initial state,
        // so we explicitly skip it and only react to *transitions* to online.
        appScope.launch {
            var first = true
            var lastOnline: Boolean? = null
            ServiceLocator.connectivityMonitor.isOnline.collectLatest { online ->
                val previous = lastOnline
                lastOnline = online
                if (first) {
                    first = false
                    Log.d(LogTags.SYNC, "Connectivity first emission online=$online")
                    return@collectLatest
                }
                if (previous != online) {
                    Log.i(LogTags.SYNC, "Connectivity transition previous=$previous → now=$online")
                    if (online) {
                        val currentUid = SessionManager.currentUserId ?: return@collectLatest
                        runCatching { ServiceLocator.syncManager.flush(currentUid) }
                            .onFailure { Log.e(LogTags.SYNC, "Connectivity flush threw", it) }
                    }
                }
            }
        }
    }
}