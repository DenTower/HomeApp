package com.example.homeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.homeapp.presentation.screens.AuthScreen
import com.example.homeapp.presentation.screens.HomeScreen
import com.example.homeapp.presentation.theme.HomeAppTheme
import dagger.hilt.android.AndroidEntryPoint

// Точка входа в приложение
@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "auth_screen",
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    composable(route = "auth_screen") {
                        AuthScreen(onNavigate = navController::navigate)
                    }
                    composable(
                        route = "main_screen/{homename}",
                        arguments = listOf(
                            navArgument(name = "homename") { type = NavType.StringType }
                        )
                    ) {
                        val homename = it.arguments?.getString("homename")!!
                        HomeScreen(homename = homename)
                    }
                }
            }
        }
    }
}