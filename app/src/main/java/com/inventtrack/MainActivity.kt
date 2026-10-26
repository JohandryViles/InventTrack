package com.inventtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.navigation.InventTrackApp
import com.inventtrack.ui.MainViewModel
import com.inventtrack.ui.theme.InventTrackTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkThemePreference by viewModel.darkTheme.collectAsStateWithLifecycle()
            val darkTheme = darkThemePreference ?: isSystemInDarkTheme()
            InventTrackTheme(darkTheme = darkTheme) {
                InventTrackApp(
                    darkTheme = darkTheme,
                    onDarkThemeChange = viewModel::setDarkTheme
                )
            }
        }
    }
}
