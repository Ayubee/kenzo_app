package com.example.kunlikvazifalar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kunlikvazifalar.theme.KunlikVazifalarTheme
import com.example.kunlikvazifalar.ui.MainViewModel
import com.example.kunlikvazifalar.ui.screens.MainAppScreen
import com.example.kunlikvazifalar.ui.screens.OnboardingScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KunlikVazifalarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppEntry(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppEntry(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.username.isNullOrBlank()) {
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
