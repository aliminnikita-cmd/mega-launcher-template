package com.megalauncher.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.megalauncher.domain.model.AppInfo
import com.megalauncher.domain.repository.AppRepository
import com.megalauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val city: String = "",
    val btMac: String? = null,
    val carPackages: List<String> = emptyList(),
    val allApps: List<AppInfo> = emptyList()
) {
    val carApps: List<AppInfo>
        get() = carPackages.mapNotNull { pkg -> allApps.firstOrNull { it.packageName == pkg } }
}

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val appRepo: AppRepository
) : ViewModel() {

    private val _all = MutableStateFlow<List<AppInfo>>(emptyList())

    val state: StateFlow<SettingsUiState> = combine(
        settings.weatherCity, settings.autoCarBluetoothMac, settings.carApps, _all
    ) { city, bt, pkgs, all ->
        SettingsUiState(city, bt, pkgs, all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init { viewModelScope.launch { _all.value = appRepo.loadAllApps() } }

    fun setCity(c: String) = viewModelScope.launch { settings.setWeatherCity(c) }
    fun setBtMac(mac: String?) = viewModelScope.launch { settings.setAutoCarBluetoothMac(mac) }

    fun addCarApp(pkg: String) {
        val current = state.value.carPackages.toMutableList()
        if (!current.contains(pkg)) {
            current.add(pkg)
            viewModelScope.launch { settings.setCarApps(current) }
        }
    }

    fun removeCarApp(pkg: String) {
        val current = state.value.carPackages.toMutableList()
        current.remove(pkg)
        viewModelScope.launch { settings.setCarApps(current) }
    }

    fun move(from: Int, to: Int) {
        val current = state.value.carPackages.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val item = current.removeAt(from)
        current.add(to, item)
        viewModelScope.launch { settings.setCarApps(current) }
    }

    class Factory(
        private val settings: SettingsRepository,
        private val appRepo: AppRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(settings, appRepo) as T
    }
}
