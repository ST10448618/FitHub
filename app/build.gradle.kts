plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)

}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\emeris\\Documents\\POE\\fithub-release.jks")
            storePassword = "Perez12"
            keyAlias = "fithub"
            keyPassword = "Perez12"
        }
    }
    namespace = "com.example.fithub"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.fithub"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://fithub-api.onrender.com/\""
            )
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        // Use for testing
        testOptions {
            unitTests.isReturnDefaultValues = true
        }

        buildFeatures {
            compose = true;
            buildConfig = true
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        configurations.all {
            // ML Kit pulls in Google's Litert which duplicates TFLite classes.
            // We only use org.tensorflow:tensorflow-lite, so exclude Litert globally.
            exclude(group = "com.google.ai.edge.litert", module = "litert-api")
            exclude(group = "com.google.ai.edge.litert", module = "litert")
            exclude(group = "com.google.ai.edge.litert", module = "litert-gpu")
            exclude(group = "com.google.ai.edge.litert", module = "litert-support")
        }


    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }


    dependencies {
        // Core
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.androidx.fragment.ktx)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.material.icons.extended)
        implementation(libs.androidx.navigation.compose)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.auth)
        implementation(libs.firebase.firestore)
        implementation(libs.firebase.messaging)
        implementation(libs.firebase.storage)

        // Room
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        ksp(libs.androidx.room.compiler)

        // Networking
        implementation(libs.retrofit)
        implementation(libs.retrofit.kotlinx.serialization)
        implementation(libs.okhttp)
        implementation(libs.okhttp.logging)
        implementation(libs.kotlinx.serialization.json)

        // ML Kit Barcode
        implementation(libs.mlkit.barcode)

        // CameraX
        implementation(libs.camera.core)
        implementation(libs.camera.camera2)
        implementation(libs.camera.lifecycle)
        implementation(libs.camera.view)

        // Biometric
        implementation(libs.androidx.biometric)

        // Coil
        implementation(libs.coil.compose)

        // DataStore
        implementation(libs.androidx.datastore.preferences)

        // WorkManager
        implementation(libs.androidx.work.runtime.ktx)


        // TensorFlow Lite
        implementation("org.tensorflow:tensorflow-lite:2.16.1")

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(libs.androidx.junit)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
        debugImplementation(libs.androidx.compose.ui.tooling)

        // PHASE 2 ADDED — Google SSO via Credential Manager
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play.services.auth)
        implementation(libs.googleid)

        testImplementation(libs.junit)
        testImplementation(libs.kotlinx.coroutines.test)

        implementation("androidx.exifinterface:exifinterface:1.3.7")

        // Phase 6B: REST API client
        implementation("com.squareup.retrofit2:retrofit:3.0.0")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
        implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    }
}