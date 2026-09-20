package com.example.resqprototype.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.resqprototype.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "resq_user_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val FULL_NAME = stringPreferencesKey("full_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val BLOOD_TYPE = stringPreferencesKey("blood_type")
        val MEDICAL_CONDITIONS = stringPreferencesKey("medical_conditions")
        val DEFAULT_NOTE = stringPreferencesKey("default_note")
        val IS_DATABASE_SEEDED = booleanPreferencesKey("is_database_seeded")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            UserProfile(
                fullName = preferences[PreferenceKeys.FULL_NAME] ?: "Juan Dela Cruz",
                phoneNumber = preferences[PreferenceKeys.PHONE_NUMBER] ?: "+63 912 345 6789",
                bloodType = preferences[PreferenceKeys.BLOOD_TYPE] ?: "O+",
                medicalConditions = preferences[PreferenceKeys.MEDICAL_CONDITIONS] ?: "Asthma",
                defaultNote = preferences[PreferenceKeys.DEFAULT_NOTE] ?: "EMERGENCY: Assistance needed in Davao City area!"
            )
        }

    val themeModeFlow: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferenceKeys.THEME_MODE] ?: "SYSTEM"
        }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }

    val isDatabaseSeededFlow: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferenceKeys.IS_DATABASE_SEEDED] ?: false
        }

    suspend fun setDatabaseSeeded(seeded: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_DATABASE_SEEDED] = seeded
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.FULL_NAME] = profile.fullName
            preferences[PreferenceKeys.PHONE_NUMBER] = profile.phoneNumber
            preferences[PreferenceKeys.BLOOD_TYPE] = profile.bloodType
            preferences[PreferenceKeys.MEDICAL_CONDITIONS] = profile.medicalConditions
            preferences[PreferenceKeys.DEFAULT_NOTE] = profile.defaultNote
        }
    }
}
