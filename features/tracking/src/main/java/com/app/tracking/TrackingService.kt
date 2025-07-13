package com.app.tracking

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.app.core.Constants
import com.app.core.TrackingUtils
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TrackingService"

/**
 * A LifecycleService that runs in the background to track the user's location.
 */
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    // Injected dependencies from Hilt modules.
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
    private var lastLocation: Location? = null
    private var inactivityJob: Job? = null

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val timeRunInSeconds = MutableLiveData<Long>()
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
        timeRunInSeconds.postValue(0L)
        distanceInMeters.postValue(0)
        speedInKMH.postValue(0f)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        // When the `isTracking` state changes, this observer triggers the necessary actions.
        isTracking.observe(this) {
            Log.d(TAG, "isTracking value changed: $it")
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }

        timeRunInSeconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L))
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
            }
        })
    }

    /**
     * Handles the logic for stopping the service completely and calculates final data that should show in history
     */
    private fun killService() {
        Log.d(TAG, "Killing service")
        serviceKilled = true
//        pauseService()
        serviceScope.launch(Dispatchers.IO) {
            pathPoints.value?.let {
                if (it.isNotEmpty() && it.last().isNotEmpty()) {
                    val trip = TripEntity(
                        startTime = timeStarted,
                        endTime = System.currentTimeMillis(),
                        distance = distanceInMeters.value?.toFloat() ?: 0f,
                        duration = timeRunInMillis.value ?: 0L
                    )
                    tripRepository.insertTrip(trip)
                    Log.d(TAG, "Trip saved: $trip")
                }
            }
        }
        postInitialValues()
        // Use STOP_FOREGROUND_REMOVE to dismiss the notification
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }
                    startTimer()
                }

                Constants.ACTION_PAUSE_SERVICE -> pauseService()
                Constants.ACTION_STOP_SERVICE -> killService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
        inactivityJob?.cancel()
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()

        serviceScope.launch {
            while (isTracking.value == true) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= (timeRunInSeconds.value!! * 1000L) + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                }
                delay(50L)
            }
            timeRun += lapTime
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            // Check for location permissions before requesting updates
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Location permission not granted. Aborting location tracking.")
                return
            }

            val isBackgroundTrackingEnabled =
                sharedPreferences.getBoolean(Constants.BACKGROUND_TRACKING_ENABLED, true)
            if (!isBackgroundTrackingEnabled) return

            val locationUpdateInterval =
                sharedPreferences.getLong(Constants.LOCATION_UPDATE_INTERVAL, 5000L)

            val request =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationUpdateInterval)
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
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * The callback that receives location updates from the FusedLocationProviderClient.
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isTracking.value == true) {
                for (location in result.locations) {
                    if (lastLocation != null) {
                        val distance = location.distanceTo(lastLocation!!)
                        if (distance < 10) { // Inactivity threshold (10 meters)
                            if (inactivityJob == null || !inactivityJob!!.isActive) {
                                inactivityJob = serviceScope.launch {
                                    delay(10000)
                                    sendInactivityNotification()
                                    killService()
//                                    pauseService() // Pause instead of killing the service
                                }
                            }
                        } else {
                            inactivityJob?.cancel()
                        }
                    }
                    lastLocation = location
                    if (location.accuracy < 50f) {
                        addPathPoint(location)
                        speedInKMH.postValue(location.speed * 3.6f)
                    }
                }
            }
        }
    }

    /**
     * Creates and displays a notification to inform the user that tracking was paused due to inactivity.
     */
    private fun sendInactivityNotification() {
        val resumeIntent = Intent(this, TrackingService::class.java).apply {
            action = Constants.ACTION_START_OR_RESUME_SERVICE
        }
        val resumePendingIntent = PendingIntent.getService(
            this, 3, resumeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder =
            NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_INACTIVITY_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle("Tracking Stopped")
                .setContentText("Tracking stopped due to inactivity.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_run, "Start Again", resumePendingIntent)

        notificationManager.notify(
            Constants.NOTIFICATION_INACTIVITY_ID,
            notificationBuilder.build()
        )
    }

    private fun addPathPoint(location: Location?) {
        location ?: return
        val newPath = pathPoints.value ?: mutableListOf()
        if (newPath.isEmpty()) {
            newPath.add(mutableListOf())
        }
        newPath.last().add(location)
        pathPoints.postValue(ArrayList(newPath)) // Ensure a new list is posted
        calculateDistance()
    }

    private fun calculateDistance() {
        val points = pathPoints.value ?: return
        var totalDistance = 0f
        points.forEach { polyline ->
            for (i in 0 until polyline.size - 1) {
                totalDistance += polyline[i].distanceTo(polyline[i + 1])
            }
        }
        distanceInMeters.postValue(totalDistance.toInt())
    }

    private fun addEmptyPolyline() {
        val currentPath = pathPoints.value ?: mutableListOf()
        // Create a new list to ensure LiveData detects the change
        val newPath = currentPath.map { it.toMutableList() }.toMutableList()
        newPath.add(mutableListOf())
        pathPoints.postValue(newPath)
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        createInactivityNotificationChannel(notificationManager)
        startForeground(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    // ✅ REWRITTEN: This function now only handles the action button state
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val actionText = if (isTracking) "Pause" else "Resume"
        val intent = Intent(this, TrackingService::class.java).apply {
            action =
                if (isTracking) Constants.ACTION_PAUSE_SERVICE else Constants.ACTION_START_OR_RESUME_SERVICE
        }
        val pendingIntent = PendingIntent.getService(
            this,
            if (isTracking) 1 else 2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Clear previous actions to avoid duplicates
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            curNotificationBuilder.addAction(R.drawable.ic_pause, actionText, pendingIntent)
            // The setContentText call was removed from here
            notificationManager.notify(Constants.NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    /**
     * Creates the notification channel for the inactivity alerts.
     */
    private fun createInactivityNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_INACTIVITY_ID,
            Constants.NOTIFICATION_CHANNEL_INACTIVITY_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Creates the notification channel for the main foreground service notification.
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}