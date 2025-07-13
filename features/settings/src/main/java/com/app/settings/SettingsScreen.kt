package com.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Fancy color palette
val PrimaryPurple = Color(0xFF6B46C1)
val SecondaryPink = Color(0xFFEC4899)
val NeutralGray = Color(0xFF6B7280)
val LightBackground = Color(0xFFF8FAFC)
val CardBackground = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Collect state from the ViewModel's StateFlows.
    val isBackgroundTrackingEnabled by viewModel.isBackgroundTrackingEnabled.collectAsState()
    val locationUpdateInterval by viewModel.locationUpdateInterval.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryPurple
                        )
                        Text(
                            "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Background Tracking Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Background Tracking",
                        fontWeight = FontWeight.Medium,
                        color = NeutralGray
                    )
                    Switch(
                        checked = isBackgroundTrackingEnabled,
                        onCheckedChange = { viewModel.updateBackgroundTracking(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryPurple,
                            checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Location Update Interval Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Location Update Interval",
                            fontWeight = FontWeight.Medium,
                            color = NeutralGray
                        )
                        Text(
                            "${locationUpdateInterval / 1000}s",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                    Slider(
                        value = (locationUpdateInterval / 1000).toFloat(),
                        onValueChange = { viewModel.updateLocationInterval(it.toLong() * 1000) },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryPurple,
                            activeTrackColor = PrimaryPurple,
                            inactiveTrackColor = PrimaryPurple.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}