package com.megalauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.megalauncher.domain.model.AppInfo
import com.megalauncher.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AppRepositoryImpl(private val context: Context) : AppRepository {

    override fun observeAllApps(): Flow<List<AppInfo>> = flow {
        emit(loadAllApps())
    }.flowOn(Dispatchers.IO)

    override fun loadAllApps(): List<AppInfo> {
        val pm = context.packageManager
        val selfPackage = context.packageName
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != selfPackage }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo),
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
