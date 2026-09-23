package com.megalauncher.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.megalauncher.domain.model.AppInfo
import com.megalauncher.domain.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppsUiState(
    val apps: List<AppInfo> = emptyList(),
    val query: String = "",
    val loading: Boolean = true
)

class AppsViewModel(private val repo: AppRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _loading = MutableStateFlow(true)
    private val _all = MutableStateFlow<List<AppInfo>>(emptyList())

    val state: StateFlow<AppsUiState> = combine(_all, _query, _loading) { apps, q, loading ->
        val filtered = if (q.isBlank()) apps
        else apps.filter { it.label.contains(q, ignoreCase = true) }
        AppsUiState(filtered, q, loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppsUiState())

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            repo.observeAllApps().collect { list ->
                _all.value = list
                _loading.value = false
            }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }

    class Factory(private val repo: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppsViewModel(repo) as T
    }
}
