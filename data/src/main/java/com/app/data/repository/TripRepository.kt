package com.app.data.repository

import com.app.data.database.LocationDao
import com.app.data.database.LocationEntity
import com.app.data.database.TripDao
import com.app.data.database.TripEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val locationDao: LocationDao,
) {

    // Trip methods
    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    fun getAllTrips(): Flow<List<TripEntity>> {
        return tripDao.getAllTrips()
    }

    suspend fun getTripById(tripId: Long): TripEntity? {
        return tripDao.getTripById(tripId)
    }

    // Location methods
    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insertLocation(location)
    }

    fun getLocationsForTrip(tripId: Long): Flow<List<LocationEntity>> {
        return locationDao.getLocationsForTrip(tripId)
    }
}