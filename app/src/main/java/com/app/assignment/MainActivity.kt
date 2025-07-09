package com.app.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.assignment.ui.theme.GPSTrackingDataCollectionAppTheme
import com.app.tracking.TrackingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSTrackingDataCollectionAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            // We can add a BottomNavigationBar here later for other features
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "tracking",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("tracking") {
                TrackingScreen()
            }
            // Add other composables for trip_history and settings later
        }
    }
}