package com.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TripEntity::class, LocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun locationDao(): LocationDao
}