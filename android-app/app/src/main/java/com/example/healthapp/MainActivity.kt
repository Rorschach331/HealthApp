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
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val authError by mainViewModel.authError.collectAsState()
    
    LaunchedEffect(authError) {
        if (authError) {
            val result = snackbarHostState.showSnackbar(
                message = "认证失效，请配置授权码",
                actionLabel = "去配置",
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                navController.navigate(Screen.Settings.route) {
                    // 避免在返回栈中堆积多个设置页
                    launchSingleTop = true
                }
                mainViewModel.clearAuthError()
            }
        }
    }
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
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
