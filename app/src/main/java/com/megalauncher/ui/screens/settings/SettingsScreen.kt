package com.megalauncher.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.megalauncher.LauncherApp
import com.megalauncher.core.ui.theme.TextSecondary
import com.megalauncher.domain.model.AppInfo
import com.megalauncher.ui.screens.car.BtPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            (LocalContext.current.applicationContext as LauncherApp).settingsRepository,
            (LocalContext.current.applicationContext as LauncherApp).appRepository
        )
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var cityInput by remember(state.city) { mutableStateOf(state.city) }
    var showBtPicker by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Настройки", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle("Погода")
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("Город (латиницей)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.setCity(cityInput) }) { Text("Сохранить город") }
            }

            item {
                SectionTitle("Bluetooth-автозапуск")
                val btLabel = state.btMac ?: "Не настроено"
                Text(btLabel, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showBtPicker = true }) { Text("Выбрать устройство") }
                    OutlinedButton(onClick = { vm.setBtMac(null) }) { Text("Сбросить") }
                }
            }

            item {
                SectionTitle("Приложения в режиме автомобиля")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showAppPicker = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Добавить")
                    }
                }
            }

            itemsIndexed(state.carApps, key = { _, app -> app.packageName }) { index, app ->
                CarAppRow(
                    app = app,
                    canUp = index > 0,
                    canDown = index < state.carApps.lastIndex,
                    onUp = { vm.move(index, index - 1) },
                    onDown = { vm.move(index, index + 1) },
                    onRemove = { vm.removeCarApp(app.packageName) }
                )
            }

            item {
                SectionTitle("Лаунчер по умолчанию")
                Text(
                    "Чтобы Mega Launcher стал домашним экраном, открой системные настройки и выбери его в разделе «Главный экран» / «Приложение по умолчанию».",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { openHomeSettings(context) }) {
                    Text("Открыть настройки лаунчера")
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showBtPicker) {
        BtPickerDialog(
            currentMac = state.btMac,
            onDismiss = { showBtPicker = false },
            onPick = { mac -> vm.setBtMac(mac); showBtPicker = false }
        )
    }

    if (showAppPicker) {
        AppPickerDialog(
            allApps = state.allApps,
            excluded = state.carPackages.toSet(),
            onDismiss = { showAppPicker = false },
            onPick = { pkg -> vm.addCarApp(pkg) }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(12.dp))
    Text(
        text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CarAppRow(
    app: AppInfo,
    canUp: Boolean,
    canDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit
) {
    val bitmap = remember(app.packageName) {
        app.icon.toBitmap(96, 96).asImageBitmap()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))
        Text(app.label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = onUp, enabled = canUp) {
            Icon(Icons.Outlined.ArrowUpward, "Вверх", tint = TextSecondary)
        }
        IconButton(onClick = onDown, enabled = canDown) {
            Icon(Icons.Outlined.ArrowDownward, "Вниз", tint = TextSecondary)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.Close, "Убрать", tint = TextSecondary)
        }
    }
}

@Composable
private fun AppPickerDialog(
    allApps: List<AppInfo>,
    excluded: Set<String>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allApps, excluded) {
        allApps.filter {
            it.packageName !in excluded && it.label.contains(query, true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить приложение") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    itemsIndexed(filtered, key = { _, a -> a.packageName }) { _, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(app.packageName) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bmp = remember(app.packageName) {
                                app.icon.toBitmap(72, 72).asImageBitmap()
                            }
                            Image(bmp, app.label, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(app.label, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

private fun openHomeSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    } else {
        Intent(Settings.ACTION_HOME_SETTINGS)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.startActivity(
            Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
