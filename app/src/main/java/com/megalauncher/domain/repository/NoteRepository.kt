package com.megalauncher.domain.repository

import com.megalauncher.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    suspend fun getById(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun delete(note: Note)
}
