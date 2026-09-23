package com.megalauncher.domain.repository

import com.megalauncher.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun observeAllApps(): Flow<List<AppInfo>>
    fun loadAllApps(): List<AppInfo>
}
