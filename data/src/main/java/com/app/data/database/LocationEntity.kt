package com.app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index // Import Index
import androidx.room.PrimaryKey

/*
 * Represents a single location data point recorded during a trip.
 * This class defines the schema for the "locations" table in the database.
 */
@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])] // Add this line
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Float
)