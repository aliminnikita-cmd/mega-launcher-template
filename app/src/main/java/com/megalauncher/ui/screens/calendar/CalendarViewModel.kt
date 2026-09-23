package com.megalauncher.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.megalauncher.domain.model.CalendarEvent
import com.megalauncher.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val events: List<CalendarEvent> = emptyList(),
    val loading: Boolean = false
)

class CalendarViewModel(private val repo: CalendarRepository) : ViewModel() {

    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state

    init { loadFor(_state.value.selectedDate) }

    fun selectDate(millis: Long) {
        _state.value = _state.value.copy(selectedDate = millis)
        loadFor(millis)
    }

    private fun loadFor(dateMillis: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val (start, end) = dayBounds(dateMillis)
            val events = withContext(Dispatchers.IO) { repo.eventsForDay(start, end) }
            _state.value = _state.value.copy(events = events, loading = false)
        }
    }

    private fun dayBounds(millis: Long): Pair<Long, Long> {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = c.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, 1)
        return start to c.timeInMillis
    }

    class Factory(private val repo: CalendarRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalendarViewModel(repo) as T
    }
}
