package com.disciplinelock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.disciplinelock.app.ui.dashboard.DashboardScreen
import com.disciplinelock.app.ui.permissions.PermissionsScreen
import com.disciplinelock.app.ui.settings.SettingsScreen
import com.disciplinelock.app.ui.theme.DisciplineLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisciplineLockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisciplineLockNavHost()
                }
            }
        }
    }
}

@Composable
fun DisciplineLockNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "permissions") {
        composable("permissions") {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate("home") {
                        popUpTo("permissions") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable(
            route = "settings",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
