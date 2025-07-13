package com.app.tracking.di

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.app.core.Constants
import com.app.tracking.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

/**
 * module for providing dependencies that are specific to a Service's lifecycle.
 * This is perfect for our tracking service, as these dependencies will be created
 * when the service starts and destroyed when the service stops.
 */

@Module
@InstallIn(ServiceComponent::class)
object TrackingModule {

    /**
     * Provides a PendingIntent that will launch our main UI when the user taps the notification.
     * @ServiceScoped means that only one instance of this PendingIntent will be created
     * for the lifecycle of the service.
     */
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ): PendingIntent {
        val launchIntent = app.packageManager.getLaunchIntentForPackage(app.packageName)
            ?.setAction("ACTION_SHOW_TRACKING_FRAGMENT")

        return PendingIntent.getActivity(
            app,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Provides a pre-configured NotificationCompat.Builder for our tracking service's foreground notification.
     * This ensures the notification has a consistent look and feel.
     */
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("GPS Tracking")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}