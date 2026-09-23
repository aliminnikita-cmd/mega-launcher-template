package com.megalauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.megalauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private val KEY_CAR_APPS = stringPreferencesKey("car_apps")
    private val KEY_BT_MAC = stringPreferencesKey("bt_mac")
    private val KEY_CITY = stringPreferencesKey("weather_city")
    private val KEY_ONBOARDED = stringPreferencesKey("onboarded")

    override val carApps: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_CAR_APPS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }

    override val autoCarBluetoothMac: Flow<String?> =
        context.dataStore.data.map { it[KEY_BT_MAC] }

    override val weatherCity: Flow<String> =
        context.dataStore.data.map { it[KEY_CITY] ?: "Moscow" }

    override val onboarded: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ONBOARDED] == "true" }

    override suspend fun setCarApps(packages: List<String>) {
        context.dataStore.edit { it[KEY_CAR_APPS] = packages.joinToString(",") }
    }

    override suspend fun setAutoCarBluetoothMac(mac: String?) {
        context.dataStore.edit { prefs ->
            if (mac == null) prefs.remove(KEY_BT_MAC) else prefs[KEY_BT_MAC] = mac
        }
    }

    override suspend fun setWeatherCity(city: String) {
        context.dataStore.edit { it[KEY_CITY] = city.trim() }
    }

    override suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = value.toString() }
    }
}
