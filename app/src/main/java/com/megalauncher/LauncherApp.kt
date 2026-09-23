package com.megalauncher

import android.app.Application
import com.megalauncher.data.local.AppDatabase
import com.megalauncher.data.repository.AppRepositoryImpl
import com.megalauncher.data.repository.CalendarRepositoryImpl
import com.megalauncher.data.repository.NoteRepositoryImpl
import com.megalauncher.data.repository.SettingsRepositoryImpl
import com.megalauncher.data.repository.WeatherRepositoryImpl
import com.megalauncher.domain.repository.AppRepository
import com.megalauncher.domain.repository.CalendarRepository
import com.megalauncher.domain.repository.NoteRepository
import com.megalauncher.domain.repository.SettingsRepository
import com.megalauncher.domain.repository.WeatherRepository

class LauncherApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }

    val appRepository: AppRepository by lazy { AppRepositoryImpl(this) }
    val noteRepository: NoteRepository by lazy { NoteRepositoryImpl(database.noteDao()) }
    val calendarRepository: CalendarRepository by lazy { CalendarRepositoryImpl(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(this) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepositoryImpl() }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LauncherApp
            private set
    }
}
