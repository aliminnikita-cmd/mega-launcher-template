package com.megalauncher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.megalauncher.ui.screens.apps.AppsScreen
import com.megalauncher.ui.screens.calendar.CalendarScreen
import com.megalauncher.ui.screens.home.HomeScreen
import com.megalauncher.ui.screens.notes.NotesScreen
import com.megalauncher.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onOpenCarMode: () -> Unit
) {
    NavHost(navController = navController, startDestination = Destination.Home.route) {
        composable(Destination.Home.route) {
            HomeScreen(onOpenSettings = { navController.navigate("settings") })
        }
        composable(Destination.Notes.route) { NotesScreen() }
        composable(Destination.Calendar.route) { CalendarScreen() }
        composable(Destination.Apps.route) { AppsScreen() }
        composable(Destination.Car.route) { onOpenCarMode() }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
