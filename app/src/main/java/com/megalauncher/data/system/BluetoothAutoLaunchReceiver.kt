package com.megalauncher.data.system

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.megalauncher.LauncherApp
import com.megalauncher.ui.screens.car.CarModeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BluetoothAutoLaunchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        val mac = device?.address ?: return

        val app = context.applicationContext as? LauncherApp ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val targetMac = app.settingsRepository.autoCarBluetoothMac.first()
            if (targetMac != null && targetMac.equals(mac, ignoreCase = true)) {
                val launch = Intent(context, CarModeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launch)
            }
        }
    }
}
