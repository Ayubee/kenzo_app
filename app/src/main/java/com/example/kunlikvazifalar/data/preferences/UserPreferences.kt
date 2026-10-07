package com.example.kunlikvazifalar.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "kunlik_vazifalar_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_NOTIF_REQUESTED = "notif_permission_requested"
        private const val KEY_THEME_MODE = "theme_mode"
    }

    fun getUsername(): String? {
        val name = prefs.getString(KEY_USERNAME, null)
        return if (name.isNullOrBlank()) null else name.trim()
    }

    fun setUsername(name: String) {
        prefs.edit().putString(KEY_USERNAME, name.trim()).apply()
    }

    fun isNotificationPermissionRequested(): Boolean {
        return prefs.getBoolean(KEY_NOTIF_REQUESTED, false)
    }

    fun setNotificationPermissionRequested(requested: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_REQUESTED, requested).apply()
    }

    fun getThemeMode(): ThemeMode = ThemeMode.entries.firstOrNull {
        it.name == prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
    } ?: ThemeMode.SYSTEM

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun repeatsEnabled() = prefs.getBoolean("repeat_reminders", true)
    fun setRepeatsEnabled(enabled: Boolean) { prefs.edit().putBoolean("repeat_reminders", enabled).apply() }
    fun animationsEnabled() = prefs.getBoolean("animations", true)
    fun setAnimationsEnabled(enabled: Boolean) { prefs.edit().putBoolean("animations", enabled).apply() }
    fun hapticsEnabled() = prefs.getBoolean("haptics", true)
    fun setHapticsEnabled(enabled: Boolean) { prefs.edit().putBoolean("haptics", enabled).apply() }
}
