package com.example.homeapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.homeapp.ui.theme.HomeAppTheme
import dagger.hilt.android.AndroidEntryPoint

// Точка входа в приложение
@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeAppTheme {
                HomeScreen()
            }
        }
    }
}