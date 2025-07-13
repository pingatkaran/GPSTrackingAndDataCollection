package com.app.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.app.core.Constants
import com.app.data.database.AppDatabase
import com.app.data.database.LocationDao
import com.app.data.database.TripDao
import com.app.data.repository.TripRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This is a Dagger Hilt module. Think of it as a factory that knows how to create
 * and provide all the dependencies related to our data layer.
 */

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Provides the SharedPreferences instance for the app.
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SETTINGS_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gps_tracking_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the DAO for accessing Trip data.
     */
    @Provides
    fun provideTripDao(appDatabase: AppDatabase): TripDao {
        return appDatabase.tripDao()
    }

    /**
     * Provides the DAO for accessing Location data.
     */
    @Provides
    fun provideLocationDao(appDatabase: AppDatabase): LocationDao {
        return appDatabase.locationDao()
    }

    /**
     * Provides our main repository.
     * ViewModels will talk to this repository, not directly to the DAOs. This keeps our
     * architecture clean and makes it easy to test.
     * Hilt provides the DAOs this repository needs in its constructor.
     */
    @Provides
    @Singleton
    fun provideTripRepository(
        tripDao: TripDao,
        locationDao: LocationDao
    ): TripRepository {
        return TripRepository(tripDao, locationDao)
    }
}