package com.example.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthapp.ui.components.BottomNavigationBar
import com.example.healthapp.ui.components.Screen
import com.example.healthapp.ui.screens.ChartScreen
import com.example.healthapp.ui.screens.HistoryScreen
import com.example.healthapp.ui.screens.InputScreen
import com.example.healthapp.ui.screens.SettingsScreen
import com.example.healthapp.ui.screens.WelcomeScreen
import com.example.healthapp.ui.theme.HealthAppTheme
import com.example.healthapp.utils.PreferenceManager
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PreferenceManager(this)
        
        // Initialize Retrofit synchronously before UI
        val baseUrl = prefs.getBaseUrl()
        if (baseUrl.isNotEmpty()) {
            RetrofitClient.setBaseUrl(baseUrl)
        }
        
        setContent {
            HealthAppTheme {
                var isConfigured by remember { mutableStateOf(!prefs.isFirstRun()) }
                
                if (!isConfigured) {
                    WelcomeScreen(onConfigured = { isConfigured = true })
                } else {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Input.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Input.route) { InputScreen(mainViewModel, navController) }
            composable(Screen.List.route) { HistoryScreen(mainViewModel) }
            composable(Screen.Chart.route) { ChartScreen(mainViewModel) }
            composable(Screen.Settings.route) { SettingsScreen(mainViewModel) }
        }
    }
}
