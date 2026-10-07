package com.example.kunlikvazifalar.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "kunlik_vazifalar_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_NOTIF_REQUESTED = "notif_permission_requested"
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
}
