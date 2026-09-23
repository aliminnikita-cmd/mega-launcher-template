package com.megalauncher.ui.screens.car

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun BtPickerDialog(
    currentMac: String?,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit
) {
    val context = LocalContext.current
    var devices by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) devices = loadPaired(context)
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) devices = loadPaired(context)
        else permLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bluetooth-устройство для автозапуска") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                TextButton(onClick = { onPick(null) }) {
                    Text("Отключить автозапуск")
                }
                Spacer(Modifier.height(8.dp))
                if (devices.isEmpty()) {
                    Text("Нет сопряжённых устройств")
                } else {
                    LazyColumn {
                        items(devices) { (mac, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(mac) }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    if (mac.equals(currentMac, true)) "● $name" else name
                                )
                            }
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

private fun loadPaired(context: Context): List<Pair<String, String>> {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val adapter: BluetoothAdapter = manager?.adapter ?: return emptyList()
    return try {
        adapter.bondedDevices?.map { it.address to (it.name ?: it.address) } ?: emptyList()
    } catch (_: SecurityException) {
        emptyList()
    }
}
