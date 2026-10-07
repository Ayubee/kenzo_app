package com.example.kunlikvazifalar.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.example.kunlikvazifalar.data.preferences.ThemeMode

/** Persist the app override with Android so the system's starting window matches too. */
object PlatformTheme {
    fun apply(context: Context, mode: ThemeMode) {
        if (Build.VERSION.SDK_INT >= 31) {
            val platformMode = when (mode) {
                ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                // AUTO clears the package's explicit night configuration in UiModeManagerService.
                ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            }
            context.getSystemService(UiModeManager::class.java)?.setApplicationNightMode(platformMode)
        }
    }
}
