package com.megalauncher.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Notes : Destination("notes", "Заметки", Icons.Outlined.EditNote)
    data object Calendar : Destination("calendar", "Календарь", Icons.Outlined.CalendarMonth)
    data object Home : Destination("home", "Главный", Icons.Outlined.Home)
    data object Car : Destination("car", "Авто", Icons.Outlined.DirectionsCar)
    data object Apps : Destination("apps", "Приложения", Icons.Outlined.Apps)
}

val bottomNavItems = listOf(
    Destination.Notes,
    Destination.Calendar,
    Destination.Home,
    Destination.Car,
    Destination.Apps
)
