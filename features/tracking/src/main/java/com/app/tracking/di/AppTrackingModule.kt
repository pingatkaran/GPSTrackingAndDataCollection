package com.app.tracking.di

import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for providing dependencies related to location tracking.
 * This module specifically handles the setup for Google Play Services' location APIs.
 *
 * @Module tells Hilt that this object is a provider of dependencies.
 * @InstallIn(SingletonComponent::class) specifies that the dependencies provided here
 * will have an application-wide scope, living as long as the app does.
 */

@Module
@InstallIn(SingletonComponent::class)
object AppTrackingModule {


    /**
     * Provides a singleton instance of FusedLocationProviderClient
     */
    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = LocationServices.getFusedLocationProviderClient(app)

}