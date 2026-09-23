package com.megalauncher.data.mapper

import com.megalauncher.data.local.entity.NoteEntity
import com.megalauncher.domain.model.Note

fun NoteEntity.toDomain(): Note =
    Note(id, title, content, createdAt, updatedAt)

fun Note.toEntity(): NoteEntity =
    NoteEntity(id, title, content, createdAt, updatedAt)
