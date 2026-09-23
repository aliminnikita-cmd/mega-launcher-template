package com.megalauncher.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.megalauncher.data.local.dao.NoteDao
import com.megalauncher.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mega_launcher.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
