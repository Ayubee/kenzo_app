package com.example.kunlikvazifalar

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.notification.NotificationHelper
import com.example.kunlikvazifalar.theme.PlatformTheme
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kunlikvazifalar.theme.KunlikVazifalarTheme
import com.example.kunlikvazifalar.ui.MainViewModel
import com.example.kunlikvazifalar.ui.screens.MainAppScreen
import com.example.kunlikvazifalar.ui.screens.OnboardingScreen
import com.example.kunlikvazifalar.data.preferences.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val mode = UserPreferences(newBase).getThemeMode()
        val base = if (mode == ThemeMode.SYSTEM || Build.VERSION.SDK_INT >= 31) newBase else {
            val configuration = Configuration(newBase.resources.configuration)
            configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (mode == ThemeMode.DARK) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            newBase.createConfigurationContext(configuration)
        }
        super.attachBaseContext(base)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        PlatformTheme.apply(this, UserPreferences(this).getThemeMode())
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleTaskIntent(intent)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SideEffect {
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
            KunlikVazifalarTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppEntry(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAfterResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTaskIntent(intent)
    }

    private fun handleTaskIntent(intent: Intent?) {
        val id = intent?.getLongExtra(NotificationHelper.EXTRA_OPEN_TASK_ID, -1) ?: -1
        if (id >= 0) viewModel.openTask(id)
        intent?.removeExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)
    }
}

@Composable
fun AppEntry(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading || uiState.loadError != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(painterResource(R.drawable.ic_kenzo), "Kenzo App", Modifier.size(72.dp))
                if (uiState.loadError != null) {
                    Text(uiState.loadError!!)
                    Button(onClick = viewModel::loadData) { Text("Qayta urinish") }
                } else {
                    CircularProgressIndicator(Modifier.size(24.dp))
                    Text("Yuklanmoqda…")
                }
            }
        }
    } else if (uiState.username.isNullOrBlank()) {
        OnboardingScreen(
            onFinishOnboarding = { username ->
                viewModel.saveUsername(username)
                viewModel.setNotificationPrompted()
            }
        )
    } else {
        MainAppScreen(viewModel = viewModel)
    }
}
