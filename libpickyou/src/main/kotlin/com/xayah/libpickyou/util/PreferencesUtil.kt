package com.xayah.libpickyou.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

enum class ThemeType {
    AUTO,
    LIGHT_THEME,
    DARK_THEME;

    companion object {
        val default: ThemeType
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AUTO else LIGHT_THEME

        fun of(name: String?): ThemeType =
            runCatching { ThemeType.valueOf(name!!.uppercase()) }.getOrDefault(default)
    }
}

internal object PreferencesUtil {
    private const val KEY_REQUESTED_ROOT = "requested_root"
    private const val KEY_THEME_TYPE = "theme_type"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    private fun getPref(): SharedPreferences = ContextUtil.getContext()!!.getSharedPreferences("PickYouPreferences", Context.MODE_PRIVATE)

    fun saveRequestedRoot(value: Boolean) = runCatching { getPref().edit().putBoolean(KEY_REQUESTED_ROOT, value).apply() }

    fun readRequestedRoot() = runCatching { getPref().getBoolean(KEY_REQUESTED_ROOT, false) }.getOrElse { false }

    fun saveDynamicColor(value: Boolean) = runCatching { getPref().edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply() }

    fun readDynamicColor() = runCatching { getPref().getBoolean(KEY_DYNAMIC_COLOR, false) }.getOrElse { true }

    fun saveThemeType(type: ThemeType) = runCatching { getPref().edit().putString(KEY_THEME_TYPE, type.name).apply() }

    fun readThemeType(): ThemeType = ThemeType.of(getPref().getString(KEY_THEME_TYPE, ""))
}
