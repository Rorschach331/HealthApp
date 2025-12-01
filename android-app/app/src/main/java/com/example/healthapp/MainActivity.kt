package com.example.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PreferenceManager(this)
        
        setContent {
            HealthAppTheme {
                var isConfigured by remember { mutableStateOf(!prefs.isFirstRun()) }
                
                if (!isConfigured) {
                    WelcomeScreen(onConfigured = { isConfigured = true })
                } else {
                    // Initialize Retrofit with stored URL
                    LaunchedEffect(Unit) {
                        val baseUrl = prefs.getBaseUrl()
                        if (baseUrl.isNotEmpty()) {
                            RetrofitClient.setBaseUrl(baseUrl)
                        }
                    }
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Input.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Input.route) { InputScreen() }
            composable(Screen.List.route) { HistoryScreen() }
            composable(Screen.Chart.route) { ChartScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
