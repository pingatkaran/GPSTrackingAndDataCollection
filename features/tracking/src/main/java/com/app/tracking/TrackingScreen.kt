package com.app.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.core.Constants
import com.app.core.TrackingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.observeAsState(initial = false)
    val pathPoints by viewModel.pathPoints.observeAsState(initial = emptyList())
    val timeRunInMillis by viewModel.timeRunInMillis.observeAsState(initial = 0L)
    val distanceInMeters by viewModel.distanceInMeters.observeAsState(initial = 0)
    val speedInKMH by viewModel.speedInKMH.observeAsState(initial = 0f)

    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(22.6, 72.8), 10f)
    }

    // This enables the blue dot and the button to center on it
    val mapProperties = MapProperties(
        isMyLocationEnabled = hasLocationPermission
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val location = viewModel.getLastKnownLocation()
            location?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f),
                    durationMs = 1500
                )
            }
        }
    }

    LaunchedEffect(pathPoints) {
        if (isTracking && pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            val lastLatLng = LatLng(pathPoints.last().last().latitude, pathPoints.last().last().longitude)
            launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLng(lastLatLng)
                )
            }
        }
    }

    if (hasLocationPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
            ) {
                pathPoints.forEach { polyline ->
                    if (polyline.size > 1) {
                        Polyline(
                            points = polyline.map { LatLng(it.latitude, it.longitude) },
                            color = MaterialTheme.colorScheme.primary,
                            width = 10f
                        )
                    }
                }
            }
            // UI Controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Metrics Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricText(
                        label = "Distance",
                        value = String.format("%.2f km", distanceInMeters / 1000f)
                    )
                    MetricText(
                        label = "Speed",
                        value = "${speedInKMH.roundToInt()} km/h"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = TrackingUtils.getFormattedStopWatchTime(timeRunInMillis),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE, context)
                        },
                        enabled = !isTracking
                    ) {
                        Text("Start")
                    }
                    Button(
                        onClick = {
                            if (isTracking) {
                                sendCommandToService(Constants.ACTION_PAUSE_SERVICE, context)
                            } else {
                                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE, context)
                            }
                        },
                        enabled = isTracking || timeRunInMillis > 0L
                    ) {
                        Text(text = if (!isTracking && timeRunInMillis > 0L) "Resume" else "Pause")
                    }
                    if (isTracking || timeRunInMillis > 0) {
                        Button(
                            onClick = { sendCommandToService(Constants.ACTION_STOP_SERVICE, context) },
                        ) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Location permission is needed to track your trips.")
        }
    }
}

@Composable
fun MetricText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun sendCommandToService(action: String, context: Context) {
    Intent(context, TrackingService::class.java).also {
        it.action = action
        context.startService(it)
    }
}