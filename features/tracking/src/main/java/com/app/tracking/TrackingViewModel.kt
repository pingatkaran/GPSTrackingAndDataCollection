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

    var isMapInitialized = false
    val isTracking = TrackingService.isTracking
    val pathPoints = TrackingService.pathPoints
    val timeRunInMillis = TrackingService.timeRunInMillis
    val distanceInMeters = TrackingService.distanceInMeters
    val speedInKMH = TrackingService.speedInKMH

    /**
     * Saves a completed trip to the database via the repository.
     * This is launched in the viewModelScope, which is a coroutine scope tied to the ViewModel's lifecycle
     */
    fun saveTrip(trip: TripEntity) = viewModelScope.launch {
        tripRepository.insertTrip(trip)
    }

    /*
    *  Asynchronously fetches the last known location from the FusedLocationProviderClient.
    */
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