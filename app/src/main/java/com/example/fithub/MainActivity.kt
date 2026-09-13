package com.example.fithub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.fithub.ui.navigation.FitHubNavGraph
import com.example.fithub.ui.navigation.Screen
import com.example.fithub.ui.theme.FitHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    FitHubNavGraph(
                        navController = navController,
                        // For now, start at Landing so you can click through.
                        // Track A will replace this with real auth-state logic.
                        startDestination = Screen.LANDING
                    )
                }
            }
        }
    }
}