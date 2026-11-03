package com.example.fithub

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.fithub.core.LogTags
import com.example.fithub.ui.navigation.FitHubNavGraph
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.theme.FitHubTheme

/**
 * Single-Activity host. Extends [androidx.fragment.app.FragmentActivity] (NOT
 * AppCompat) because the app theme uses `android:Theme.Material.Light.NoActionBar`.
 * Never convert this to AppCompatActivity — Phase 5 locale switching uses the
 * `attachBaseContext` wrapper pattern which works from either base, but the
 * Material theme itself is not an AppCompat descendant.
 */
class MainActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(LogTags.APP, "MainActivity.onCreate")
        enableEdgeToEdge()
        setContent {
            FitHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    FitHubNavGraph(
                        navController = navController,
                        startDestination = Screen.SPLASH
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(LogTags.APP, "MainActivity.onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(LogTags.APP, "MainActivity.onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(LogTags.APP, "MainActivity.onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LogTags.APP, "MainActivity.onDestroy")
    }
}