package com.sakinah.app.feature.prayer

import android.hardware.GeomagneticField
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object QiblaCalculator {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the Great-Circle initial bearing from the observer's location to the Kaaba in Mecca.
     * Result is in degrees [0, 360).
     */
    fun calculateBearing(userLat: Double, userLng: Double): Float {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return ((bearingDeg + 360.0) % 360.0).toFloat()
    }

    /**
     * Corrects magnetic azimuth to true geographic azimuth using GeomagneticField declination.
     */
    fun calculateTrueAzimuth(
        magneticAzimuth: Float,
        userLat: Double,
        userLng: Double,
        altitudeMeters: Double = 0.0,
        timeMillis: Long = System.currentTimeMillis()
    ): Float {
        return try {
            val geoField = GeomagneticField(
                userLat.toFloat(),
                userLng.toFloat(),
                altitudeMeters.toFloat(),
                timeMillis
            )
            ((magneticAzimuth + geoField.declination + 360f) % 360f)
        } catch (e: Exception) {
            magneticAzimuth
        }
    }

    /**
     * Calculates the shortest signed turn angle from device heading to Qibla bearing.
     * Returns a value in [-180, 180]:
     *  > 0 means turn right
     *  < 0 means turn left
     *  ~ 0 means aligned
     */
    fun calculateShortestDifference(deviceHeading: Float, qiblaBearing: Float): Float {
        var diff = (qiblaBearing - deviceHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Circular angle smoothing (low-pass filter on degrees).
     */
    fun smoothAngle(current: Float, target: Float, alpha: Float = 0.18f): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (current + alpha * diff + 360f) % 360f
    }

    /**
     * Human-readable guidance text for turning towards Qibla.
     */
    fun getGuidanceText(deviceHeading: Float, qiblaBearing: Float, threshold: Float = 3.0f): Pair<String, Boolean> {
        val diff = calculateShortestDifference(deviceHeading, qiblaBearing)
        return when {
            kotlin.math.abs(diff) <= threshold -> {
                "أنت بمواجهة القبلة تماماً ✓" to true
            }
            diff > 0 -> {
                "دُر إلى اليمين بمقدار ${diff.roundToInt()}°" to false
            }
            else -> {
                "دُر إلى اليسار بمقدار ${(-diff).roundToInt()}°" to false
            }
        }
    }
}
