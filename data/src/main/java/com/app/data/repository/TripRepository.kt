package com.app.data.repository

import com.app.data.database.LocationDao
import com.app.data.database.LocationEntity
import com.app.data.database.TripDao
import com.app.data.database.TripEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * The Repository class acts for trip-related data.
 * It abstracts the data sources from the rest of the app,
 * like the ViewModels. This means the ViewModel doesn't know or care if the data
 * is coming from a local database, a remote server, or a cache.
 *
 */
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val locationDao: LocationDao,
) {

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

    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insertLocation(location)
    }

    fun getLocationsForTrip(tripId: Long): Flow<List<LocationEntity>> {
        return locationDao.getLocationsForTrip(tripId)
    }
}