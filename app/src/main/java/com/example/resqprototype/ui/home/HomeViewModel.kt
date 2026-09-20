package com.example.resqprototype.ui.home

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqprototype.data.ble.BleAdvertiserManager
import com.example.resqprototype.data.location.LocationManager
import com.example.resqprototype.data.preferences.UserPreferencesRepository
import com.example.resqprototype.domain.model.SosAlert
import com.example.resqprototype.domain.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = UserPreferencesRepository(application)
    private val advertiserManager = BleAdvertiserManager(application)
    private val locationManager = LocationManager(application)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _customNote = MutableStateFlow("")
    val customNote: StateFlow<String> = _customNote.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = _isBroadcasting.asStateFlow()

    private val _lastBroadcastTime = MutableStateFlow<Long?>(null)
    val lastBroadcastTime: StateFlow<Long?> = _lastBroadcastTime.asStateFlow()

    val currentLocation: StateFlow<Location> = locationManager.currentLocation

    val isHardwareBleSupported: Boolean
        get() = advertiserManager.isBleAdvertisingSupported()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            prefsRepository.userProfileFlow.collect { savedProfile ->
                _userProfile.value = savedProfile
                if (_customNote.value.isEmpty()) {
                    _customNote.value = savedProfile.defaultNote
                }
            }
        }

        viewModelScope.launch {
            prefsRepository.themeModeFlow.collect { mode ->
                _themeMode.value = mode
            }
        }

        locationManager.requestCurrentLocation()
    }

    fun updateCustomNote(newNote: String) {
        _customNote.value = newNote
        val updated = _userProfile.value.copy(defaultNote = newNote)
        _userProfile.value = updated
        debounceSaveProfile(updated)
    }

    fun updateFullProfile(newProfile: UserProfile) {
        _userProfile.value = newProfile
        viewModelScope.launch {
            prefsRepository.saveUserProfile(newProfile)
        }
    }

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch {
            prefsRepository.saveThemeMode(mode)
        }
    }

    private fun debounceSaveProfile(profile: UserProfile) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            prefsRepository.saveUserProfile(profile)
        }
    }

    fun toggleSosBroadcast() {
        _isBroadcasting.value = !_isBroadcasting.value
        if (_isBroadcasting.value) {
            locationManager.requestCurrentLocation()
            _lastBroadcastTime.value = System.currentTimeMillis()

            val currentProfile = _userProfile.value
            val liveLoc = locationManager.currentLocation.value

            val alert = SosAlert(
                senderName = currentProfile.fullName,
                latitude = liveLoc.latitude,
                longitude = liveLoc.longitude,
                customMessage = _customNote.value,
                medicalNotes = "Blood Type: ${currentProfile.bloodType}"
            )

            // Start physical BLE advertising (or fallback to emulator simulation)
            advertiserManager.startAdvertising(alert)

            viewModelScope.launch {
                prefsRepository.saveUserProfile(currentProfile)
            }
        } else {
            advertiserManager.stopAdvertising()
        }
    }
}
