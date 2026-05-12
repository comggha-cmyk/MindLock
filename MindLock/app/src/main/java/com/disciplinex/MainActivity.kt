package com.disciplinex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.disciplinex.ui.screens.*
import com.disciplinex.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Alarms    : Screen("alarms",    "Alarm", Icons.Default.Alarm)
    object AppBlock  : Screen("appblock",  "Block", Icons.Default.Block)
    object Focus     : Screen("focus",     "Focus", Icons.Default.Timer)
    object Goals     : Screen("goals",     "Goals", Icons.Default.EmojiEvents)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Alarms,
    Screen.Focus,
    Screen.AppBlock,
    Screen.Goals
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisciplineXTheme {
                DisciplineXApp()
            }
        }
    }
}

@Composable
fun DisciplineXApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = DXColors.Background,
        bottomBar = {
            DXBottomNav(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(200)) + slideInHorizontally { it / 10 } },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Alarms.route)    { AlarmScreen() }
            composable(Screen.Focus.route)     { FocusScreen() }
            composable(Screen.AppBlock.route)  { AppBlockScreen() }
            composable(Screen.Goals.route)     { GoalsScreen() }
        }
    }
}

@Composable
fun DXBottomNav(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = DXColors.Surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        screen.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DXColors.Primary,
                    selectedTextColor = DXColors.Primary,
                    indicatorColor = DXColors.PrimaryContainer,
                    unselectedIconColor = DXColors.OnBackgroundMuted,
                    unselectedTextColor = DXColors.OnBackgroundMuted
                )
            )
        }
    }
}
