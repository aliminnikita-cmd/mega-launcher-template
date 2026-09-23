package com.megalauncher.ui.screens.car

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.megalauncher.LauncherApp
import com.megalauncher.core.ui.theme.CarBg
import com.megalauncher.core.ui.theme.CarSurface
import com.megalauncher.domain.model.AppInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CarModeScreen(
    onExit: () -> Unit,
    vm: CarModeViewModel = viewModel(
        factory = CarModeViewModel.Factory(
            (LocalContext.current.applicationContext as LauncherApp).appRepository,
            (LocalContext.current.applicationContext as LauncherApp).settingsRepository
        )
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showBtPicker by remember { mutableStateOf(false) }

    var now by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) { now = Calendar.getInstance().time; delay(1000L) }
    }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("d MMMM", Locale("ru")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarBg)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    timeFmt.format(now),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(dateFmt.format(now), fontSize = 14.sp, color = Color(0xFFAAAAAA))
            }
            IconButton(onClick = { showBtPicker = true }) {
                Icon(Icons.Outlined.Bluetooth, "Bluetooth", tint = Color.White)
            }
            IconButton(onClick = vm::toggleConfigMode) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Настроить",
                    tint = if (state.configMode) Color(0xFFFFB300) else Color.White
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        val items = if (state.configMode) state.allApps else state.selectedApps

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.packageName }) { app ->
                CarAppTile(
                    app = app,
                    configMode = state.configMode,
                    selected = state.selectedPackages.contains(app.packageName),
                    onClick = {
                        if (state.configMode) {
                            vm.toggleApp(app.packageName)
                        } else {
                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }
        }
    }

    if (showBtPicker) {
        BtPickerDialog(
            currentMac = state.btMac,
            onDismiss = { showBtPicker = false },
            onPick = { mac ->
                vm.setBtMac(mac)
                showBtPicker = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarAppTile(
    app: AppInfo,
    configMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bitmap = remember(app.packageName) {
        app.icon.toBitmap(160, 160).asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CarSurface)
            .combinedClickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                app.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
        }
        if (configMode && selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x33FFB300))
            )
        }
    }
}
