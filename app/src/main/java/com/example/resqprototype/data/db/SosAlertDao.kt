package com.example.resqprototype.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SosAlertDao {

    /**
     * Returns all active or resolved SOS alerts (excluding archived alerts),
     * sorted by nearest distance first, then newest timestamp.
     */
    @Query("SELECT * FROM sos_alerts WHERE status != 'ARCHIVED' ORDER BY distanceMeters ASC, timestamp DESC")
    fun getAllAlertsSortedByDistance(): Flow<List<SosAlertEntity>>

    /**
     * Inserts or updates an SOS alert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAlert(alert: SosAlertEntity)

    /**
     * Updates an alert's status ("ACTIVE", "RESOLVED", "ARCHIVED").
     */
    @Query("UPDATE sos_alerts SET status = :status WHERE sosId = :sosId")
    suspend fun updateAlertStatus(sosId: String, status: String)

    /**
     * Soft-deletes (archives) an alert by changing its status to "ARCHIVED".
     */
    @Query("UPDATE sos_alerts SET status = 'ARCHIVED' WHERE sosId = :sosId")
    suspend fun archiveAlertById(sosId: String)

    /**
     * Archives all currently active alerts en masse (Soft Delete).
     */
    @Query("UPDATE sos_alerts SET status = 'ARCHIVED' WHERE status != 'ARCHIVED'")
    suspend fun archiveAllAlerts()

    /**
     * Hard-deletes all alerts from SQLite storage.
     */
    @Query("DELETE FROM sos_alerts")
    suspend fun clearAllAlerts()
}
