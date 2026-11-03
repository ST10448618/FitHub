package com.example.fithub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.fithub.core.LogTags
import com.example.fithub.core.notifications.NotificationHelper
import com.example.fithub.core.notifications.PendingRoute
import com.example.fithub.ui.navigation.FitHubNavGraph
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.theme.FitHubTheme

class MainActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(LogTags.APP, "MainActivity.onCreate")
        handleIntent(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val route = intent?.getStringExtra(NotificationHelper.EXTRA_ROUTE)
        if (route != null) {
            Log.d(LogTags.PUSH, "MainActivity received notification deep link: $route")
            PendingRoute.set(route)
        }
    }

    override fun onStart() { super.onStart(); Log.d(LogTags.APP, "MainActivity.onStart") }
    override fun onResume() { super.onResume(); Log.d(LogTags.APP, "MainActivity.onResume") }
    override fun onPause() { super.onPause(); Log.d(LogTags.APP, "MainActivity.onPause") }
    override fun onDestroy() { super.onDestroy(); Log.d(LogTags.APP, "MainActivity.onDestroy") }
}