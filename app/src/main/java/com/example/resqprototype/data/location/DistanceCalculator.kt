package com.example.resqprototype.data.location

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance in meters between two lat/lng points using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_METERS * c).toFloat()
    }

    /**
     * Formats distance into human-readable string (e.g. "120 m" or "1.5 km").
     */
    fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000f)
        }
    }
}
