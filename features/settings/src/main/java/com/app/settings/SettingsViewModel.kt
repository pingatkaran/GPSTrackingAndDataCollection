package com.app.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isBackgroundTrackingEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("background_tracking_enabled", true) // Default to true
    )
    val isBackgroundTrackingEnabled: StateFlow<Boolean> = _isBackgroundTrackingEnabled

    private val _locationUpdateInterval = MutableStateFlow(
        sharedPreferences.getLong("location_update_interval", 5000L)
    )
    val locationUpdateInterval: StateFlow<Long> = _locationUpdateInterval

    fun updateBackgroundTracking(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("background_tracking_enabled", isEnabled).apply()
        _isBackgroundTrackingEnabled.value = isEnabled
    }

    fun updateLocationInterval(interval: Long) {
        sharedPreferences.edit().putLong("location_update_interval", interval).apply()
        _locationUpdateInterval.value = interval
    }
}