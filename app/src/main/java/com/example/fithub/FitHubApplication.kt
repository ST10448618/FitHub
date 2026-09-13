package com.example.fithub

import android.app.Application
import com.example.fithub.core.ServiceLocator
import com.example.fithub.data.seed.DemoDataSeeder
import com.google.firebase.FirebaseApp
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
        FirebaseApp.initializeApp(this)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        ServiceLocator.init(this)

        appScope.launch {
            // 1. Seed verified plans + exercises (needed by both tracks)
            ServiceLocator.seedManager.seedIfNeeded()

            // 2. Seed demo user data for the shared containers
            DemoDataSeeder.seedIfNeeded(ServiceLocator.database)

            // 3. Quick verification logs
            android.util.Log.d(
                "FitHubSeed",
                "USER: " + ServiceLocator.database.userProfileDao().getById("demo_user")
            )
            android.util.Log.d(
                "FitHubSeed",
                "VERIFIED PLANS: " + ServiceLocator.database.workoutPlanDao().verifiedCount()
            )
        }
    }
}