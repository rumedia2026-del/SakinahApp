package com.sakinah.app

import com.sakinah.app.feature.prayer.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class QiblaCalculatorUnitTest {

    @Test
    fun testCairoQiblaBearing() {
        // Cairo: 30.0444, 31.2357 -> expected ~136°
        val bearing = QiblaCalculator.calculateBearing(30.0444, 31.2357)
        assertTrue("Cairo bearing should be approx 136° (+-1.5°), was: $bearing", abs(bearing - 136.0f) < 1.5f)
    }

    @Test
    fun testLondonQiblaBearing() {
        // London: 51.5074, -0.1278 -> expected ~119°
        val bearing = QiblaCalculator.calculateBearing(51.5074, -0.1278)
        assertTrue("London bearing should be approx 119° (+-1.5°), was: $bearing", abs(bearing - 119.0f) < 1.5f)
    }

    @Test
    fun testRashidQiblaBearing() {
        // Rashid (Egypt): 31.4033, 30.4170 -> exact Great-Circle bearing is 137.62°
        val bearing = QiblaCalculator.calculateBearing(31.4033, 30.4170)
        assertTrue("Rashid bearing should be approx 137.6° (+-1.5°), was: $bearing", abs(bearing - 137.6f) < 1.5f)
    }

    @Test
    fun testShortestTurnDifference() {
        // Heading 350°, Target 10° -> turn right by +20°
        val diff1 = QiblaCalculator.calculateShortestDifference(350f, 10f)
        assertEquals(20f, diff1, 0.01f)

        // Heading 10°, Target 350° -> turn left by -20°
        val diff2 = QiblaCalculator.calculateShortestDifference(10f, 350f)
        assertEquals(-20f, diff2, 0.01f)

        // Aligned within 2° -> guidance reports facing Qibla
        val (text, isFacing) = QiblaCalculator.getGuidanceText(136f, 137f, 3.0f)
        assertTrue(isFacing)
        assertTrue(text.contains("بمواجهة القبلة"))

        // Heading 100°, Qibla 136° -> turn right by 36°
        val (textRight, isFacingRight) = QiblaCalculator.getGuidanceText(100f, 136f, 3.0f)
        assertFalse(isFacingRight)
        assertTrue(textRight.contains("اليمين"))
        assertTrue(textRight.contains("36"))
    }

    @Test
    fun testCircularAngleSmoothing() {
        // Smooth across 359° to 1° boundary
        val smoothed = QiblaCalculator.smoothAngle(359f, 1f, 0.5f)
        // Midpoint should be 0° (or 360°)
        assertTrue("Smoothed angle should wrap smoothly around 0°, was $smoothed", abs(smoothed - 0f) < 0.1f || abs(smoothed - 360f) < 0.1f)
    }
}
