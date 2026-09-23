package com.megalauncher.data.repository

import com.megalauncher.data.local.dao.NoteDao
import com.megalauncher.data.mapper.toDomain
import com.megalauncher.data.mapper.toEntity
import com.megalauncher.domain.model.Note
import com.megalauncher.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(private val dao: NoteDao) : NoteRepository {
    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Note? = dao.getById(id)?.toDomain()
    override suspend fun upsert(note: Note): Long = dao.upsert(note.toEntity())
    override suspend fun delete(note: Note) = dao.delete(note.toEntity())
}
