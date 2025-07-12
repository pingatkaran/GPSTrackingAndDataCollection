package com.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingUtilsTest {

    @Test
    fun getFormattedStopWatchTime_withMillis_returnsCorrectlyFormattedString() {
        val ms = 123456L
        val expected = "00:02:03:45"
        val result = TrackingUtils.getFormattedStopWatchTime(ms, true)
        assertEquals(expected, result)
    }

    @Test
    fun getFormattedStopWatchTime_withoutMillis_returnsCorrectlyFormattedString() {
        val ms = 789012L
        val expected = "00:13:09"
        val result = TrackingUtils.getFormattedStopWatchTime(ms, false)
        assertEquals(expected, result)
    }
}