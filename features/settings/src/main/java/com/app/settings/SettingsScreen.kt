package com.app.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isBackgroundTrackingEnabled by viewModel.isBackgroundTrackingEnabled.collectAsState()
    val locationUpdateInterval by viewModel.locationUpdateInterval.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        // Background Tracking Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Background Tracking")
            Switch(
                checked = isBackgroundTrackingEnabled,
                onCheckedChange = { viewModel.updateBackgroundTracking(it) }
            )
        }

        // Location Update Interval
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Location Update Interval")
            // In a real app, you would use a DropdownMenu or other selector here
            Text("${locationUpdateInterval / 1000}s")
        }
    }
}