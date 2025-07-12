import com.app.data.database.LocationDao
import com.app.data.database.LocationEntity
import com.app.data.database.TripDao
import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class TripRepositoryTest {

    @Mock
    private lateinit var tripDao: TripDao

    @Mock
    private lateinit var locationDao: LocationDao

    private lateinit var repository: TripRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TripRepository(tripDao, locationDao)
    }

    @Test
    fun `test insertTrip`() = runTest {
        val trip = TripEntity(startTime = 1L)
        repository.insertTrip(trip)
        verify(tripDao).insertTrip(trip)
    }

    @Test
    fun `test updateTrip`() = runTest {
        val trip = TripEntity(id = 1, startTime = 1L)
        repository.updateTrip(trip)
        verify(tripDao).updateTrip(trip)
    }

    @Test
    fun `test getAllTrips`() = runTest {
        val trips = listOf(TripEntity(startTime = 1L))
        `when`(tripDao.getAllTrips()).thenReturn(flowOf(trips))
        val result = repository.getAllTrips().first()
        assertEquals(trips, result)
    }

    @Test
    fun `test getTripById`() = runTest {
        val tripId = 1L
        val trip = TripEntity(id = tripId, startTime = 1L)
        `when`(tripDao.getTripById(tripId)).thenReturn(trip)
        val result = repository.getTripById(tripId)
        assertEquals(trip, result)
    }

    @Test
    fun `test insertLocation`() = runTest {
        val location = LocationEntity(tripId = 1L, timestamp = 1L, latitude = 0.0, longitude = 0.0, speed = 0f)
        repository.insertLocation(location)
        verify(locationDao).insertLocation(location)
    }

    @Test
    fun `test getLocationsForTrip`() = runTest {
        val tripId = 1L
        val locations = listOf(LocationEntity(tripId = tripId, timestamp = 1L, latitude = 0.0, longitude = 0.0, speed = 0f))
        `when`(locationDao.getLocationsForTrip(tripId)).thenReturn(flowOf(locations))
        val result = repository.getLocationsForTrip(tripId).first()
        assertEquals(locations, result)
    }
}