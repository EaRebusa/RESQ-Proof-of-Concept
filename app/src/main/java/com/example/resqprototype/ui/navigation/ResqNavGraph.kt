package com.example.resqprototype.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.resqprototype.ui.alerts.AlertListScreen
import com.example.resqprototype.ui.home.HomeScreen
import com.example.resqprototype.ui.map.MapScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Sender : Screen("sender", "Sender Mode", Icons.Default.CellTower)
    object Rescuer : Screen("rescuer", "Rescuer Feed", Icons.Default.NotificationsActive)
    object Map : Screen("map", "Offline Map", Icons.Default.Map)
}

@Composable
fun ResqMainContainer() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val items = listOf(
        Screen.Sender,
        Screen.Rescuer,
        Screen.Map
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute?.startsWith(screen.route) == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sender.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Sender.route) {
                HomeScreen(
                    onSosTriggered = {
                        Toast.makeText(
                            context,
                            "SOS Signal Broadcast Started (Davao Location)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            composable(Screen.Rescuer.route) {
                AlertListScreen(
                    onViewMapClicked = { alert ->
                        navController.navigate("${Screen.Map.route}?alertId=${alert.id}")
                    }
                )
            }
            composable(
                route = "${Screen.Map.route}?alertId={alertId}",
                arguments = listOf(navArgument("alertId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val alertId = backStackEntry.arguments?.getString("alertId")
                MapScreen(initialFocusedAlertId = alertId)
            }
        }
    }
}
