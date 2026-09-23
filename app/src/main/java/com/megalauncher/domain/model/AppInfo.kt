package com.megalauncher.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystem: Boolean
) {
    override fun equals(other: Any?): Boolean =
        other is AppInfo && other.packageName == packageName
    override fun hashCode(): Int = packageName.hashCode()
}
