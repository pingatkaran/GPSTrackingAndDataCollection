package com.app.core

object Constants {

    // Service
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val NOTIFICATION_CHANNEL_INACTIVITY_ID = "inactivity_channel"
    const val NOTIFICATION_CHANNEL_INACTIVITY_NAME = "Inactivity"
    const val NOTIFICATION_INACTIVITY_ID = 2

    //Settings Pref
    const val SETTINGS_PREFS = "settings_prefs"
    const val BACKGROUND_TRACKING_ENABLED = "background_tracking_enabled"
    const val LOCATION_UPDATE_INTERVAL = "location_update_interval"

}