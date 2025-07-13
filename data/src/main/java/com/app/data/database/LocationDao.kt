package com.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the 'locations' table.
 * This interface defines all the database interactions (like queries and inserts)
 * related to location data.
 */
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getLocationsForTrip(tripId: Long): Flow<List<LocationEntity>>
}