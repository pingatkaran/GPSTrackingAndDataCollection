package com.app.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.app.core.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * The ViewModel for the Settings screen. It's responsible for loading, exposing,
 * and updating user-configurable settings stored in SharedPreferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isBackgroundTrackingEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(Constants.BACKGROUND_TRACKING_ENABLED, true) // Default to true
    )
    val isBackgroundTrackingEnabled: StateFlow<Boolean> = _isBackgroundTrackingEnabled

    private val _locationUpdateInterval = MutableStateFlow(
        sharedPreferences.getLong(Constants.LOCATION_UPDATE_INTERVAL, 5000L)
    )
    val locationUpdateInterval: StateFlow<Long> = _locationUpdateInterval

    fun updateBackgroundTracking(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.BACKGROUND_TRACKING_ENABLED, isEnabled).apply()
        _isBackgroundTrackingEnabled.value = isEnabled
    }

    fun updateLocationInterval(interval: Long) {
        sharedPreferences.edit().putLong(Constants.LOCATION_UPDATE_INTERVAL, interval).apply()
        _locationUpdateInterval.value = interval
    }
}