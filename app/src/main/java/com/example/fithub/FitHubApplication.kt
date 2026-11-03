package com.example.fithub

import android.app.Application
import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitHubApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.i(LogTags.APP, "Application.onCreate — process start")

        FirebaseApp.initializeApp(this)

        // ============================================================
        // MUST RUN FIRST: ServiceLocator.init() populates the `appContext`
        // lateinit property and creates the Room database. Everything below
        // (preferencesManager, repositories, etc.) relies on this.
        // ============================================================
        ServiceLocator.init(this)

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
        //     before, preserving the original "cold start = logged out"
        //     guarantee for prototype-style usage and after explicit logout.
        //
        // Must run BEFORE any screen reads SessionManager.currentUserId.
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
        }
    }
}