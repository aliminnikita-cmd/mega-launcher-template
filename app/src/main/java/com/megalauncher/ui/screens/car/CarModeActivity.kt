package com.megalauncher.ui.screens.car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.megalauncher.core.ui.theme.MegaLauncherTheme

class CarModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MegaLauncherTheme {
                CarModeScreen(onExit = { finish() })
            }
        }
    }
}
