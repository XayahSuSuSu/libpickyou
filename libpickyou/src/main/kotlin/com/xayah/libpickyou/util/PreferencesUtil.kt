package com.xayah.libpickyou.util

import android.content.Context
import android.content.SharedPreferences
import com.xayah.libpickyou.PickYouApplication

internal object PreferencesUtil {
    private const val KeyRequestedRoot = "requested_root"
    private fun getPref(): SharedPreferences = PickYouApplication.application.getSharedPreferences("PickYouPreferences", Context.MODE_PRIVATE)

    fun saveRequestedRoot(value: Boolean) = getPref().edit().putBoolean(KeyRequestedRoot, value).apply()

    fun readRequestedRoot() = getPref().getBoolean(KeyRequestedRoot, false)
}