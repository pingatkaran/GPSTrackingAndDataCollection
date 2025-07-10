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
}