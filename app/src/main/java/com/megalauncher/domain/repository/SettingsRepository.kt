package com.megalauncher.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val carApps: Flow<List<String>>
    val autoCarBluetoothMac: Flow<String?>
    val weatherCity: Flow<String>
    val onboarded: Flow<Boolean>

    suspend fun setCarApps(packages: List<String>)
    suspend fun setAutoCarBluetoothMac(mac: String?)
    suspend fun setWeatherCity(city: String)
    suspend fun setOnboarded(value: Boolean)
}
