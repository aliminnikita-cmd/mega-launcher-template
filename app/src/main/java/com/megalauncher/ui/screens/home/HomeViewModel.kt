package com.megalauncher.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.megalauncher.domain.model.CalendarEvent
import com.megalauncher.domain.model.Weather
import com.megalauncher.domain.repository.CalendarRepository
import com.megalauncher.domain.repository.SettingsRepository
import com.megalauncher.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val events: List<CalendarEvent> = emptyList(),
    val weather: Weather? = null,
    val weatherLoading: Boolean = false,
    val weatherError: String? = null
)

class HomeViewModel(
    private val calendarRepo: CalendarRepository,
    private val weatherRepo: WeatherRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        refreshEvents()
        refreshWeather()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            val events = withContext(Dispatchers.IO) { calendarRepo.upcomingEvents(5) }
            _state.value = _state.value.copy(events = events)
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _state.value = _state.value.copy(weatherLoading = true, weatherError = null)
            val city = settings.weatherCity.first()
            val weather = withContext(Dispatchers.IO) { weatherRepo.getCurrent(city) }
            _state.value = _state.value.copy(
                weather = weather,
                weatherLoading = false,
                weatherError = if (weather == null) "Не удалось загрузить погоду" else null
            )
        }
    }

    class Factory(
        private val calendarRepo: CalendarRepository,
        private val weatherRepo: WeatherRepository,
        private val settings: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(calendarRepo, weatherRepo, settings) as T
    }
}
