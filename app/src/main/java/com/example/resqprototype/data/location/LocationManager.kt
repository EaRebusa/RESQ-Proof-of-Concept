package com.example.resqprototype.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Default to Davao City coordinates (7.0736, 125.6110)
    private val defaultDavaoLocation = Location("default").apply {
        latitude = 7.0736
        longitude = 125.6110
    }

    private val _currentLocation = MutableStateFlow<Location>(defaultDavaoLocation)
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation() {
        if (!hasLocationPermission()) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _currentLocation.value = location
                }
            }
            .addOnFailureListener {
                // Keep default Davao City location on failure
            }
    }
}
