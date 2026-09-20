package com.example.resqprototype.ui.alerts

import android.app.Application
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqprototype.data.ble.BleScannerManager
import com.example.resqprototype.data.db.ResqDatabase
import com.example.resqprototype.data.location.LocationManager
import com.example.resqprototype.data.preferences.UserPreferencesRepository
import com.example.resqprototype.data.repository.SosRepository
import com.example.resqprototype.domain.model.SosAlert
import com.example.resqprototype.service.SosBleScannerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SosRepository(ResqDatabase.getInstance(application).sosAlertDao())
    private val prefsRepository = UserPreferencesRepository(application)
    private val scannerManager = BleScannerManager(application)
    private val locationManager = LocationManager(application)

    private val _alerts = MutableStateFlow<List<SosAlert>>(emptyList())
    val alerts: StateFlow<List<SosAlert>> = _alerts.asStateFlow()

    val rescuerLocation: StateFlow<Location> = locationManager.currentLocation

    val isHardwareBleScannerSupported: Boolean
        get() = scannerManager.isBleScanningSupported()

    init {
        viewModelScope.launch {
            repository.allAlerts.collect { alertList ->
                val isSeeded = prefsRepository.isDatabaseSeededFlow.first()
                if (alertList.isEmpty() && !isSeeded) {
                    prefsRepository.setDatabaseSeeded(true)
                    seedDefaultAlerts()
                } else {
                    _alerts.value = alertList
                }
            }
        }

        locationManager.requestCurrentLocation()
        startBleForegroundScanner()
    }

    private fun startBleForegroundScanner() {
        try {
            val intent = Intent(getApplication(), SosBleScannerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(getApplication(), intent)
            } else {
                getApplication<Application>().startService(intent)
            }
        } catch (e: Exception) {
            // Safe fallback on emulator
        }
    }

    private suspend fun seedDefaultAlerts() {
        val sample1 = SosAlert(
            id = "sos_matina_01",
            senderName = "Juan Dela Cruz",
            latitude = 7.0736,
            longitude = 125.6110,
            customMessage = "Floodwaters rising rapidly near Matina Crossing! Need boat assistance.",
            medicalNotes = "Blood Type: O+ | Asthma",
            status = "ACTIVE"
        )
        val sample2 = SosAlert(
            id = "sos_poblacion_02",
            senderName = "Maria Santos",
            latitude = 7.0644,
            longitude = 125.5975,
            customMessage = "Trapped in building ground floor. Structural debris blocking door.",
            medicalNotes = "Blood Type: A+ | Diabetic",
            status = "ACTIVE"
        )
        repository.saveAlert(sample1)
        repository.saveAlert(sample2)
    }

    fun addMockAlert(name: String, note: String) {
        viewModelScope.launch {
            val rawAlert = SosAlert(
                id = "sos_device_${(1..100).random()}",
                senderName = name,
                latitude = 7.0700 + (Math.random() * 0.015 - 0.0075),
                longitude = 125.6000 + (Math.random() * 0.015 - 0.0075),
                customMessage = note,
                medicalNotes = "Blood Type: O+",
                status = "ACTIVE"
            )
            repository.saveAlert(rawAlert)
        }
    }

    fun markAlertResolved(sosId: String) {
        viewModelScope.launch {
            repository.markAlertResolved(sosId)
        }
    }

    fun archiveAlert(sosId: String) {
        viewModelScope.launch {
            repository.archiveAlert(sosId)
        }
    }

    fun archiveAllAlerts() {
        viewModelScope.launch {
            repository.archiveAllAlerts()
        }
    }
}
