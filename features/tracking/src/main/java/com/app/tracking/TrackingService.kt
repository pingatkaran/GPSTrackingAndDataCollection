
package com.app.tracking

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.app.core.Constants
import com.app.core.TrackingUtils
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TrackingService"

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var tripRepository: TripRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    private var serviceKilled = false
    private var isFirstRun = true
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var timeRun = 0L
    private var lapTime = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<MutableList<MutableList<Location>>>()
        val distanceInMeters = MutableLiveData<Int>()
        val speedInKMH = MutableLiveData<Float>()
    }

    private fun postInitialValues() {
        Log.d(TAG, "Posting initial values")
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        distanceInMeters.postValue(0)
        speedInKMH.postValue(0f)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        isTracking.observe(this) {
            Log.d(TAG, "isTracking value changed: $it")
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    private fun killService() {
        Log.d(TAG, "Killing service")
        serviceKilled = true
        pauseService()
        // Save the trip before killing the service
        serviceScope.launch(Dispatchers.IO) {
            val trip = TripEntity(
                startTime = timeStarted,
                endTime = System.currentTimeMillis(),
                distance = distanceInMeters.value?.toFloat() ?: 0f,
                duration = timeRunInMillis.value ?: 0L
            )
            tripRepository.insertTrip(trip)
            Log.d(TAG, "Trip saved: $trip")
        }
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d(TAG, "Received Start or Resume Service action")
                    if (isFirstRun) {
                        Log.d(TAG, "First run - starting foreground service")
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Log.d(TAG, "Resuming tracking")
                    }
                    startTimer()
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Log.d(TAG, "Received Pause Service action")
                    pauseService()
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Log.d(TAG, "Received Stop Service action")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        Log.d(TAG, "Pausing service")
        isTracking.postValue(false)
    }

    private fun startTimer() {
        Log.d(TAG, "Starting timer, current isTracking: ${isTracking.value}")

        // Add empty polyline if needed
        if (pathPoints.value.isNullOrEmpty() || pathPoints.value?.last()?.isNotEmpty() == true) {
            addEmptyPolyline()
        }

        // Set tracking to true
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()

        serviceScope.launch {
            while (isTracking.value == true) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    curNotificationBuilder.setContentText(
                        TrackingUtils.getFormattedStopWatchTime(timeRunInMillis.value!!)
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(Constants.NOTIFICATION_ID, curNotificationBuilder.build())
                    lastSecondTimestamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
            Log.d(TAG, "Timer stopped, total time: $timeRun")
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        Log.d(TAG, "updateLocationTracking called with: $isTracking")

        if (isTracking) {
            val isBackgroundTrackingEnabled = sharedPreferences.getBoolean("background_tracking_enabled", true)
            if (!isBackgroundTrackingEnabled) {
                Log.d(TAG, "Background tracking disabled, not starting location updates")
                return
            }

            Log.d(TAG, "Starting location updates")
            val locationUpdateInterval = sharedPreferences.getLong("location_update_interval", 5000L)

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationUpdateInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationUpdateInterval / 2)
                .setMaxUpdateDelayMillis(locationUpdateInterval * 2)
                .build()

            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.d(TAG, "Stopping location updates")
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            Log.d(TAG, "Location result received, isTracking: ${isTracking.value}")

            if (isTracking.value == true) {
                result.locations.let { locations ->
                    for (location in locations) {
                        Log.d(TAG, "New location: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}")

                        // Only add location if accuracy is reasonable (less than 50 meters)
                        if (location.accuracy < 50f) {
                            addPathPoint(location)
                            speedInKMH.postValue(location.speed * 3.6f)
                        } else {
                            Log.d(TAG, "Location accuracy too low: ${location.accuracy}, skipping")
                        }
                    }
                }
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
            Log.d(TAG, "Location availability: ${locationAvailability.isLocationAvailable}")
        }
    }

    private fun addPathPoint(location: Location?) {
        location ?: return

        val currentPathPoints = pathPoints.value
        if (currentPathPoints != null && currentPathPoints.isNotEmpty()) {
            val currentPath = currentPathPoints.last()
            currentPath.add(location)
            pathPoints.postValue(currentPathPoints)
            calculateDistance()
        } else {
            Log.d(TAG, "No path points available, creating new polyline")
            addEmptyPolyline()
            addPathPoint(location)
        }
    }

    private fun calculateDistance() {
        var totalDistance = 0f
        pathPoints.value?.forEach { polyline ->
            if (polyline.size > 1) {
                for (i in 0 until polyline.size - 1) {
                    totalDistance += polyline[i].distanceTo(polyline[i + 1])
                }
            }
        }
        distanceInMeters.postValue(totalDistance.toInt())
    }

    private fun addEmptyPolyline() {
        Log.d(TAG, "Adding empty polyline")
        val currentPathPoints = pathPoints.value
        if (currentPathPoints != null) {
            currentPathPoints.add(mutableListOf())
            pathPoints.postValue(currentPathPoints)
        } else {
            pathPoints.postValue(mutableListOf(mutableListOf()))
        }
    }

    private fun startForegroundService() {
        Log.d(TAG, "Starting foreground service")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        startForeground(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(Constants.NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        // The constant name was wrong here, it should be NOTIFICATION_CHANNEL_NAME
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME, // This was the typo
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}