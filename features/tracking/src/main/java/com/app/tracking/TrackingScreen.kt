package com.app.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.core.Constants
import com.app.core.TrackingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons

// Using the same fancy color palette
val PrimaryPurple = Color(0xFF6B46C1)
val SecondaryPink = Color(0xFFEC4899)
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)
val NeutralGray = Color(0xFF6B7280)
val LightBackground = Color(0xFFF8FAFC)
val CardBackground = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Check for permission status
    var hasLocationPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Track if we've requested permissions to avoid repeated requests
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        permissionRequested = true
    }

    // Request permissions only once if we don't have them
    LaunchedEffect(Unit) {
        if (!hasLocationPermission && !permissionRequested) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    // Background with light color
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryPurple.copy(alpha = 0.1f),
                                            SecondaryPink.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "Tracking Screen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                windowInsets = WindowInsets(0.dp)
            )

            // Content
            if (hasLocationPermission) {
                TrackingScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onSendCommand = { action ->
                        sendCommandToService(action, context)
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryPurple.copy(alpha = 0.1f),
                                        SecondaryPink.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Location permission is needed to track your trips.",
                        textAlign = TextAlign.Center,
                        color = NeutralGray,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Add a button to manually request permissions again
                    TextButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                        }
                    ) {
                        Text("Grant Permission", color = PrimaryPurple)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingScreenContent(
    modifier: Modifier = Modifier,
    viewModel: TrackingViewModel,
    onSendCommand: (String) -> Unit
) {
    val isTracking by viewModel.isTracking.observeAsState(initial = false)
    val pathPoints by viewModel.pathPoints.observeAsState(initial = emptyList())
    val timeRunInMillis by viewModel.timeRunInMillis.observeAsState(initial = 0L)
    val distanceInMeters by viewModel.distanceInMeters.observeAsState(initial = 0)
    val speedInKMH by viewModel.speedInKMH.observeAsState(initial = 0f)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(22.6, 72.8), 10f)
    }

    LaunchedEffect(Unit) {
        if (!viewModel.isMapInitialized) {
            viewModel.getLastKnownLocation()?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 15f),
                    durationMs = 1500
                )
            }
            viewModel.isMapInitialized = true
        }
    }

    LaunchedEffect(pathPoints) {
        if (isTracking && pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            pathPoints.last().lastOrNull()?.let {
                cameraPositionState.animate(update = CameraUpdateFactory.newLatLng(it.toLatLng()))
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metrics Card
        MetricsCard(
            speed = speedInKMH,
            distance = distanceInMeters,
            elapsedTime = timeRunInMillis
        )

        // Map Area
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true
                )
            ) {
                pathPoints.forEach { polyline ->
                    if (polyline.size > 1) {
                        Polyline(
                            points = polyline.map { it.toLatLng() },
                            color = PrimaryPurple,
                            width = 12f
                        )
                    }
                }
            }
        }

        // Control Buttons
        Controls(
            isTracking = isTracking,
            hasStarted = timeRunInMillis > 0L,
            onSendCommand = onSendCommand
        )
    }
}

@Composable
fun MetricsCard(speed: Float, distance: Int, elapsedTime: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricDisplay(
                value = String.format("%.1f", speed),
                unit = "km/h",
                label = "SPEED",
                color = AccentBlue
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(
                        color = NeutralGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(0.5.dp)
                    )
            )

            MetricDisplay(
                value = String.format("%.1f", distance / 1000f),
                unit = "km",
                label = "DISTANCE",
                color = AccentGreen
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(
                        color = NeutralGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(0.5.dp)
                    )
            )

            MetricDisplay(
                value = TrackingUtils.getFormattedStopWatchTime(elapsedTime, false),
                unit = "time",
                label = "DURATION",
                color = PrimaryPurple
            )
        }
    }
}

@Composable
fun Controls(isTracking: Boolean, hasStarted: Boolean, onSendCommand: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show Start button only if tracking has not started yet
        if (!hasStarted) {
            TextButton(
                onClick = {
                    onSendCommand(Constants.ACTION_START_OR_RESUME_SERVICE)
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaIcon(
                        faIcon = FaIcons.Play,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Show Pause and Stop buttons when tracking is active
        if (isTracking) {
            TextButton(onClick = { onSendCommand(Constants.ACTION_PAUSE_SERVICE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaIcon(
                        faIcon = FaIcons.Pause,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Pause",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            TextButton(onClick = { onSendCommand(Constants.ACTION_STOP_SERVICE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaIcon(
                        faIcon = FaIcons.Stop,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Stop",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Show Resume and Stop buttons when tracking is paused
        if (!isTracking && hasStarted) {
            TextButton(onClick = { onSendCommand(Constants.ACTION_START_OR_RESUME_SERVICE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaIcon(
                        faIcon = FaIcons.Play,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Resume",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            TextButton(onClick = { onSendCommand(Constants.ACTION_STOP_SERVICE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaIcon(
                        faIcon = FaIcons.Stop,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Stop",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricDisplay(label: String, value: String, unit: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = NeutralGray,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = NeutralGray,
            textAlign = TextAlign.Center
        )
    }
}

private fun android.location.Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

private fun sendCommandToService(action: String, context: Context) {
    Intent(context, TrackingService::class.java).also {
        it.action = action
        context.startService(it)
    }
}