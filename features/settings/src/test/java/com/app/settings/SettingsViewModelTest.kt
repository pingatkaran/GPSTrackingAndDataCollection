import android.content.SharedPreferences
import com.app.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class SettingsViewModelTest {

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)
        `when`(editor.putLong(anyString(), anyLong())).thenReturn(editor)
        viewModel = SettingsViewModel(sharedPreferences)
    }

    @Test
    fun `test updateBackgroundTracking`() = runTest {
        val isEnabled = true
        viewModel.updateBackgroundTracking(isEnabled)
        verify(editor).putBoolean("background_tracking_enabled", isEnabled)
        verify(editor).apply()
        assertEquals(isEnabled, viewModel.isBackgroundTrackingEnabled.first())
    }

    @Test
    fun `test updateLocationInterval`() = runTest {
        val interval = 10000L
        viewModel.updateLocationInterval(interval)
        verify(editor).putLong("location_update_interval", interval)
        verify(editor).apply()
        assertEquals(interval, viewModel.locationUpdateInterval.first())
    }
}