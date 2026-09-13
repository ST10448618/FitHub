package com.example.fithub

import android.app.Application
import com.example.fithub.data.local.FitHubDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FitHubApplication : Application() {

    /** Single shared Room database instance for the whole app. */
    val database: FitHubDatabase by lazy {
        FitHubDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialise Firebase once at app startup
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}