package com.app.trip_history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.MockitoAnnotations
import java.io.File

@ExperimentalCoroutinesApi
class TripHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Mock
    private lateinit var tripRepository: TripRepository

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: TripHistoryViewModel

    private lateinit var fileProviderMock: MockedStatic<FileProvider>
    private lateinit var intentMock: MockedStatic<Intent>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Mock static methods for Android framework classes
        fileProviderMock = Mockito.mockStatic(FileProvider::class.java)
        intentMock = Mockito.mockStatic(Intent::class.java)
    }

    @After
    fun tearDown() {
        // Release the static mocks
        fileProviderMock.close()
        intentMock.close()
    }

    @Test
    fun `test allTrips flow is collected correctly`() = runTest {
        val trips = listOf(TripEntity(startTime = 1L), TripEntity(startTime = 2L))
        whenever(tripRepository.getAllTrips()).thenReturn(flowOf(trips))

        // Initialize ViewModel here to use the mock setup for this specific test
        viewModel = TripHistoryViewModel(tripRepository)

        // Await the first non-initial emission from the flow
        val result = viewModel.allTrips.first { it.isNotEmpty() }

        assertEquals(trips, result)
    }
}
