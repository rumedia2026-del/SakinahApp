package com.sakinah.app

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.sakinah.app.feature.prayer.PrayerTimesHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PrayerSchedulingUnitTest {

    @Test
    fun testCairoCalculationMethodIsEgyptian() {
        val cairo = PrayerTimesHelper.getCityByName("القاهرة")
        assertEquals("القاهرة", cairo.name)
        assertEquals(com.batoulapps.adhan.CalculationMethod.EGYPTIAN, cairo.calculationMethod)
        assertEquals(30.0444, cairo.coordinates.latitude, 0.001)
        assertEquals(31.2357, cairo.coordinates.longitude, 0.001)
    }

    @Test
    fun testRashidCityCoordinatesAndMethod() {
        val rashid = PrayerTimesHelper.getCityByName("رشيد")
        assertTrue(rashid.name.contains("رشيد"))
        assertEquals(com.batoulapps.adhan.CalculationMethod.EGYPTIAN, rashid.calculationMethod)
        assertEquals(31.4033, rashid.coordinates.latitude, 0.01)
        assertEquals(30.4170, rashid.coordinates.longitude, 0.01)
    }

    @Test
    fun testPrayerTimesCalculationOrder() {
        val cairo = PrayerTimesHelper.getCityByName("القاهرة")
        val dc = DateComponents(2026, 11, 14)
        val params = cairo.calculationMethod.parameters.apply {
            madhab = Madhab.SHAFI
        }
        val pt = PrayerTimes(cairo.coordinates, dc, params)

        assertNotNull(pt.fajr)
        assertNotNull(pt.sunrise)
        assertNotNull(pt.dhuhr)
        assertNotNull(pt.asr)
        assertNotNull(pt.maghrib)
        assertNotNull(pt.isha)

        assertTrue("Fajr must be before Sunrise", pt.fajr.before(pt.sunrise))
        assertTrue("Sunrise must be before Dhuhr", pt.sunrise.before(pt.dhuhr))
        assertTrue("Dhuhr must be before Asr", pt.dhuhr.before(pt.asr))
        assertTrue("Asr must be before Maghrib", pt.asr.before(pt.maghrib))
        assertTrue("Maghrib must be before Isha", pt.maghrib.before(pt.isha))
    }
}
