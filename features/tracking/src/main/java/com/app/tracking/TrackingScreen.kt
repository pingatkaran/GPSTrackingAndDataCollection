package com.app.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.core.Constants
import com.app.core.TrackingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasLocationPermission by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    LaunchedEffect(true) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    // Top App Bar
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF6B46C1),
                        modifier = Modifier.size(20.dp) // Smaller icon
                    )
                    Text(
                        "GPS Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, // Smaller font size
                        color = Color.Black
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { /* TODO: Dark mode toggle */ },
                    modifier = Modifier.size(40.dp) // Smaller button
                ) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF6B46C1),
                        modifier = Modifier.size(20.dp) // Smaller icon
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            // This is the key fix - using windowInsets = WindowInsets(0.dp) removes extra padding
            windowInsets = WindowInsets(0.dp)
        )

        // Content
        if (hasLocationPermission) {
            TrackingScreenContent(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onSendCommand = { action -> sendCommandToService(action, context) }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Location permission is needed to track your trips.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
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
        viewModel.getLastKnownLocation()?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 15f),
                durationMs = 1500
            )
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
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE5E7EB)
            )
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
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
                            color = Color(0xFF6B46C1),
                            width = 10f
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
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricDisplay(
                value = String.format("%.1f", speed),
                unit = "km/h",
                label = "SPEED"
            )

            // Vertical divider
            Divider(
                modifier = Modifier
                    .height(25.dp)
                    .width(1.dp),
                color = Color(0xFFE5E7EB)
            )

            MetricDisplay(
                value = String.format("%.1f", distance / 1000f),
                unit = "km",
                label = "DISTANCE"
            )

            // Vertical divider
            Divider(
                modifier = Modifier
                    .height(25.dp)
                    .width(1.dp),
                color = Color(0xFFE5E7EB)
            )

            MetricDisplay(
                value = TrackingUtils.getFormattedStopWatchTime(elapsedTime, false),
                unit = "hh:mm",
                label = "TIME"
            )
        }
    }
}

@Composable
fun Controls(isTracking: Boolean, hasStarted: Boolean, onSendCommand: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start/Play Button
        FloatingActionButton(
            onClick = {
                if (!isTracking) {
                    onSendCommand(Constants.ACTION_START_OR_RESUME_SERVICE)
                }
            },
            shape = CircleShape,
            containerColor = Color(0xFF4ADE80), // Green
            contentColor = Color.White,
            modifier = Modifier.size(64.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(30.dp)
            )
        }

        // Pause Button (Primary - Larger)
        FloatingActionButton(
            onClick = {
                if (isTracking) {
                    onSendCommand(Constants.ACTION_PAUSE_SERVICE)
                }
            },
            shape = CircleShape,
            containerColor = Color(0xFF6B46C1), // Purple
            contentColor = Color.White,
            modifier = Modifier.size(80.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Pause",
                modifier = Modifier.size(36.dp)
            )
        }

        // Stop Button
        FloatingActionButton(
            onClick = {
                if (hasStarted) {
                    onSendCommand(Constants.ACTION_STOP_SERVICE)
                }
            },
            shape = CircleShape,
            containerColor = Color(0xFFEF4444), // Red
            contentColor = Color.White,
            modifier = Modifier.size(64.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Stop",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MetricDisplay(label: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = Color(0xFF6B7280),
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