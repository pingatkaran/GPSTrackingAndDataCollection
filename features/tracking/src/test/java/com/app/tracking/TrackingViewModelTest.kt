import com.app.data.database.TripEntity
import com.app.data.repository.TripRepository
import com.app.tracking.MainDispatcherRule
import com.app.tracking.TrackingViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class TrackingViewModelTest {

    // Apply the rule here
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var tripRepository: TripRepository

    @Mock
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var viewModel: TrackingViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = TrackingViewModel(tripRepository, fusedLocationProviderClient)
    }

    @Test
    fun `test saveTrip`() = runTest {
        val trip = TripEntity(startTime = 1L)
        viewModel.saveTrip(trip)
        verify(tripRepository).insertTrip(trip)
    }

    // ... other tests
}