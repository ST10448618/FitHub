package com.example.fithub.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.fithub.ui.components.BottomNavBar
import com.example.fithub.ui.components.BottomNavItem
import com.example.fithub.ui.components.SyncStatusBanner

/**
 * Wraps the app content with the permanent bottom navigation bar
 * only on routes that are part of the main app shell.
 *
 * Phase 3: a [SyncStatusBanner] renders above the content on **every** route
 * (including auth/onboarding) because the user may be offline before
 * authenticating. The banner hides itself when online + nothing pending.
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    val showBottomBar = currentRoute != null && currentRoute in Screen.routesWithBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onSelect = { item: BottomNavItem ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            SyncStatusBanner()
            content(padding)
        }
    }
}