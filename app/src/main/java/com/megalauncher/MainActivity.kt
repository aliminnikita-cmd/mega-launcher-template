package com.megalauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.megalauncher.core.ui.theme.MegaLauncherTheme
import com.megalauncher.ui.navigation.AppNavGraph
import com.megalauncher.ui.navigation.Destination
import com.megalauncher.ui.navigation.bottomNavItems
import com.megalauncher.ui.screens.car.CarModeActivity
import com.megalauncher.ui.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MegaLauncherTheme {
                val app = applicationContext as LauncherApp
                var onboarded by remember { mutableStateOf<Boolean?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    onboarded = app.settingsRepository.onboarded.first()
                }

                when (onboarded) {
                    null -> Box(Modifier)
                    false -> OnboardingScreen(
                        onDone = {
                            scope.launch {
                                app.settingsRepository.setOnboarded(true)
                                onboarded = true
                            }
                        }
                    )
                    true -> LauncherRoot(
                        onOpenCarMode = {
                            startActivity(Intent(this, CarModeActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LauncherRoot(onOpenCarMode: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            when (item) {
                                is Destination.Car -> onOpenCarMode()
                                else -> if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Destination.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AppNavGraph(navController = navController, onOpenCarMode = onOpenCarMode)
        }
    }
}
