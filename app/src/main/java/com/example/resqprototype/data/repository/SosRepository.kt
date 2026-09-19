package com.example.resqprototype.data.repository

import com.example.resqprototype.data.db.SosAlertDao
import com.example.resqprototype.data.db.toDomainModel
import com.example.resqprototype.data.db.toEntity
import com.example.resqprototype.data.location.DistanceCalculator
import com.example.resqprototype.domain.model.SosAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SosRepository(
    private val sosAlertDao: SosAlertDao
) {

    /**
     * Retrieves active/resolved SOS alerts from Room DB mapped back to domain models.
     */
    val allAlerts: Flow<List<SosAlert>> = sosAlertDao.getAllAlertsSortedByDistance().map { entities ->
        entities.map { it.toDomainModel() }
    }

    /**
     * Saves or updates an SOS alert. Automatically computes distance relative to rescuer.
     */
    suspend fun saveAlert(
        alert: SosAlert,
        rescuerLat: Double = 7.0736,
        rescuerLng: Double = 125.6110
    ) {
        val distance = DistanceCalculator.calculateDistanceMeters(
            lat1 = rescuerLat,
            lon1 = rescuerLng,
            lat2 = alert.latitude,
            lon2 = alert.longitude
        )
        val entity = alert.toEntity(distanceMeters = distance)
        sosAlertDao.insertOrUpdateAlert(entity)
    }

    suspend fun markAlertResolved(sosId: String) {
        sosAlertDao.updateAlertStatus(sosId, "RESOLVED")
    }

    suspend fun archiveAlert(sosId: String) {
        sosAlertDao.archiveAlertById(sosId)
    }

    suspend fun archiveAllAlerts() {
        sosAlertDao.archiveAllAlerts()
    }

    suspend fun clearAllAlertsHard() {
        sosAlertDao.clearAllAlerts()
    }
}
