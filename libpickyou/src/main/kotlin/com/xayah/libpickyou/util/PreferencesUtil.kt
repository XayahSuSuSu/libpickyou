package com.xayah.libpickyou.util

import android.content.Context
import android.content.SharedPreferences

internal object PreferencesUtil {
    private const val KeyRequestedRoot = "requested_root"
    private fun getPref(): SharedPreferences =
        ContextUtil.getContext()!!.getSharedPreferences("PickYouPreferences", Context.MODE_PRIVATE)

    fun saveRequestedRoot(value: Boolean) =
        runCatching {
            getPref().edit().putBoolean(KeyRequestedRoot, value).apply()
        }

    fun readRequestedRoot() =
        runCatching {
            getPref().getBoolean(KeyRequestedRoot, false)
        }.getOrElse { false }
}
