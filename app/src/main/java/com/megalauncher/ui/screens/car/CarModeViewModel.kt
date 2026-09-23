package com.megalauncher.ui.screens.car

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

data class CarUiState(
    val allApps: List<AppInfo> = emptyList(),
    val selectedPackages: List<String> = emptyList(),
    val configMode: Boolean = false,
    val btMac: String? = null
) {
    val selectedApps: List<AppInfo>
        get() = selectedPackages.mapNotNull { pkg -> allApps.firstOrNull { it.packageName == pkg } }
}

class CarModeViewModel(
    private val appRepo: AppRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _all = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _config = MutableStateFlow(false)

    val state: StateFlow<CarUiState> = combine(
        _all, settings.carApps, _config, settings.autoCarBluetoothMac
    ) { all, selected, cfg, bt ->
        CarUiState(
            allApps = all,
            selectedPackages = if (selected.isEmpty()) defaultSelection(all) else selected,
            configMode = cfg,
            btMac = bt
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarUiState())

    init {
        viewModelScope.launch { _all.value = appRepo.loadAllApps() }
    }

    fun toggleConfigMode() { _config.value = !_config.value }

    fun toggleApp(pkg: String) {
        val current = state.value.selectedPackages.toMutableList()
        if (current.contains(pkg)) current.remove(pkg) else current.add(pkg)
        viewModelScope.launch { settings.setCarApps(current) }
    }

    fun move(from: Int, to: Int) {
        val current = state.value.selectedPackages.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val item = current.removeAt(from)
        current.add(to, item)
        viewModelScope.launch { settings.setCarApps(current) }
    }

    fun setBtMac(mac: String?) {
        viewModelScope.launch { settings.setAutoCarBluetoothMac(mac) }
    }

    private fun defaultSelection(all: List<AppInfo>): List<String> {
        val preferred = listOf(
            "com.google.android.apps.maps",
            "com.spotify.music",
            "com.android.dialer"
        )
        val found = all.filter { it.packageName in preferred }.map { it.packageName }
        return if (found.isNotEmpty()) found else all.take(4).map { it.packageName }
    }

    class Factory(
        private val appRepo: AppRepository,
        private val settings: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CarModeViewModel(appRepo, settings) as T
    }
}
