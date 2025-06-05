package com.example.nereu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Destinations {
    const val HOME_SCREEN = "home_screen"
    const val GRAPH_SCREEN = "graph_screen"
}

class MainActivity : ComponentActivity() {

    private val viewModel: NereuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Destinations.HOME_SCREEN
            ) {
                composable(Destinations.HOME_SCREEN) {
                    NereuApp(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable(Destinations.GRAPH_SCREEN) {
                    NereuGraphScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}