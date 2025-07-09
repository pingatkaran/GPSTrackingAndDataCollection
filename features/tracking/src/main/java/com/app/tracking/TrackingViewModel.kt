package com.app.tracking

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val isTracking = TrackingService.isTracking
    val pathPoints = TrackingService.pathPoints
    val timeRunInMillis = TrackingService.timeRunInMillis
    val distanceInMeters = TrackingService.distanceInMeters // Expose distance
    val speedInKMH = TrackingService.speedInKMH             // Expose speed

    fun saveTrip(trip: TripEntity) = viewModelScope.launch {
        tripRepository.insertTrip(trip)
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }.addOnCanceledListener {
                continuation.cancel()
            }
        }
    }
}