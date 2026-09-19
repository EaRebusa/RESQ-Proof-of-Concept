package com.example.resqprototype.ui.map

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqprototype.data.db.ResqDatabase
import com.example.resqprototype.data.location.LocationManager
import com.example.resqprototype.data.repository.SosRepository
import com.example.resqprototype.domain.model.SosAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SosRepository(ResqDatabase.getInstance(application).sosAlertDao())
    private val locationManager = LocationManager(application)

    private val _alerts = MutableStateFlow<List<SosAlert>>(emptyList())
    val alerts: StateFlow<List<SosAlert>> = _alerts.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _focusedAlert = MutableStateFlow<SosAlert?>(null)
    val focusedAlert: StateFlow<SosAlert?> = _focusedAlert.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allAlerts.collect { alertList ->
                _alerts.value = alertList
            }
        }

        viewModelScope.launch {
            locationManager.currentLocation.collect { location ->
                _userLocation.value = location
            }
        }

        locationManager.requestCurrentLocation()
    }

    fun focusAlert(alert: SosAlert) {
        _focusedAlert.value = alert
    }

    fun clearFocus() {
        _focusedAlert.value = null
    }
}
