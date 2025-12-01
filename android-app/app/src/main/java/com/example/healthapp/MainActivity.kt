package com.example.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthapp.ui.components.BottomNavigationBar
import com.example.healthapp.ui.components.Screen
import com.example.healthapp.ui.screens.ChartScreen
import com.example.healthapp.ui.screens.HistoryScreen
import com.example.healthapp.ui.screens.InputScreen
import com.example.healthapp.ui.theme.HealthAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthAppTheme {
                MainScreen()
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
        }
    }
}
