package com.megalauncher.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.megalauncher.domain.model.Note
import com.megalauncher.domain.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repo: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(note: Note, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = repo.upsert(
                note.copy(
                    createdAt = if (note.id == 0L) now else note.createdAt,
                    updatedAt = now
                )
            )
            onSaved(id)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch { repo.delete(note) }
    }

    class Factory(private val repo: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotesViewModel(repo) as T
    }
}
