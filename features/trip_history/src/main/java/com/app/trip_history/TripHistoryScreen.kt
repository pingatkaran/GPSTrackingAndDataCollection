package com.app.trip_history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.core.TrackingUtils
import com.app.data.database.TripEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripHistoryScreen(
    viewModel: TripHistoryViewModel = hiltViewModel()
) {
    val trips by viewModel.allTrips.collectAsState()

    if (trips.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No trips recorded yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trips) { trip ->
                TripItem(trip = trip)
            }
        }
    }
}

@Composable
fun TripItem(trip: TripEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trip started on: ${formatTimestamp(trip.startTime)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Duration: ${TrackingUtils.getFormattedStopWatchTime(trip.duration)}")
                Text(String.format("Distance: %.2f km", trip.distance / 1000f))
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}