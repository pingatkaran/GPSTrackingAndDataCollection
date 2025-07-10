package com.app.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
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

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gps_tracking_database"
        )
            // If you still see the error, you can add .fallbackToDestructiveMigration()
            // This will clear the database on a schema change.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTripDao(appDatabase: AppDatabase): TripDao {
        return appDatabase.tripDao()
    }

    @Provides
    fun provideLocationDao(appDatabase: AppDatabase): LocationDao {
        return appDatabase.locationDao()
    }

    @Provides
    @Singleton
    fun provideTripRepository(
        tripDao: TripDao,
        locationDao: LocationDao
    ): TripRepository {
        return TripRepository(tripDao, locationDao)
    }
}