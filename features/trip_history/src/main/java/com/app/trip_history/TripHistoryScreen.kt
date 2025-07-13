package com.app.trip_history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.core.TrackingUtils
import com.app.data.database.TripEntity
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.guru.fontawesomecomposelib.FaIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

val PrimaryPurple = Color(0xFF6B46C1)
val SecondaryPink = Color(0xFFEC4899)
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val NeutralGray = Color(0xFF6B7280)
val LightBackground = Color(0xFFF8FAFC)
val CardBackground = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    viewModel: TripHistoryViewModel = hiltViewModel()
) {
    val trips by viewModel.allTrips.collectAsState()
    val showDialog = remember { mutableStateOf(false) } // State to control dialog visibility
    val context = LocalContext.current // Get context for the ViewModel

    // Show the dialog when `showDialog` is true
    if (showDialog.value) {
        ExportDialog(
            onDismissRequest = { showDialog.value = false },
            onExportCsv = {
                showDialog.value = false
                viewModel.exportTripsToCsv(context, trips)
            },
            onExportJson = {
                showDialog.value = false
                viewModel.exportTripsToJson(context, trips)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FaIcon(
                            faIcon = FaIcons.History,
                            tint = PrimaryPurple
                        )
                        Text(
                            "Trip History",
                            fontSize = 20.sp, // Adjusted size
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDialog.value = true },
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, SecondaryPink)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        FaIcon(
                            FaIcons.Share,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(paddingValues)
        ) {
            if (trips.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips) { trip ->
                        TripItem(trip = trip)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
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
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No trips recorded yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start your first trip to see it here",
                fontSize = 14.sp,
                color = NeutralGray
            )
        }
    }
}

@Composable
fun TripItem(trip: TripEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(PrimaryPurple, SecondaryPink)
                                ),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Trip #${trip.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
                Text(
                    text = formatDate(trip.startTime),
                    fontSize = 12.sp,
                    color = NeutralGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Started at ${formatStartTime(trip.startTime)}",
                    fontSize = 12.sp,
                    color = NeutralGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripStatItem(
                    icon = FaIcons.ClockRegular,
                    label = "Duration",
                    value = TrackingUtils.getFormattedStopWatchTime(trip.duration),
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(
                            color = NeutralGray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(0.5.dp)
                        )
                )

                TripStatItem(
                    icon = FaIcons.Route,
                    label = "Distance",
                    value = String.format("%.1f km", trip.distance / 1000f),
                    color = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TripStatItem(
    icon: FaIconType,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            FaIcon(
                faIcon = icon,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = NeutralGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ExportDialog(
    onDismissRequest: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Export Trip Data", fontWeight = FontWeight.Bold, color = PrimaryPurple)
        },
        text = {
            Column {
                Text(
                    text = "1. CSV",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExportCsv)
                        .padding(vertical = 12.dp),
                    fontSize = 16.sp
                )
                Text(
                    text = "2. JSON",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExportJson)
                        .padding(vertical = 12.dp),
                    fontSize = 16.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = NeutralGray)
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatStartTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}