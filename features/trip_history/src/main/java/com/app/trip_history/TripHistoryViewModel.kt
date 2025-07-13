package com.app.trip_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

@HiltViewModel
class TripHistoryViewModel @Inject constructor(
    tripRepository: TripRepository
) : ViewModel() {

    val allTrips: StateFlow<List<TripEntity>> = tripRepository.getAllTrips()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun exportTripsToCsv(context: Context, trips: List<TripEntity>) {
        val csvHeader = "id,startTime,duration,distanceInMeters\n"
        val csvData = trips.joinToString(separator = "\n") {
            "${it.id},${it.startTime},${it.duration},${it.distance}"
        }
        val fullCsv = csvHeader + csvData
        shareData(context, "trips.csv", "text/csv", fullCsv)
    }

    fun exportTripsToJson(context: Context, trips: List<TripEntity>) {
        val jsonData = trips.joinToString(prefix = "[\n", postfix = "\n]", separator = ",\n") {
            """    {
        |        "id": ${it.id},
        |        "startTime": ${it.startTime},
        |        "duration": ${it.duration},
        |        "distanceInMeters": ${it.distance}
        |    }""".trimMargin()
        }
        shareData(context, "trips.json", "application/json", jsonData)
    }

    private fun shareData(context: Context, fileName: String, mimeType: String, content: String) {
        try {
            val cachePath = File(context.cacheDir, "exports/")
            cachePath.mkdirs()

            val file = File(cachePath, fileName)
            file.writeText(content)

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Export Trips")
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            // Handle exceptions, e.g., show a toast message
            e.printStackTrace()
        }
    }
}