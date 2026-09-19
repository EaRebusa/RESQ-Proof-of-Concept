package com.example.resqprototype.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.resqprototype.domain.model.SosAlert

@Entity(tableName = "sos_alerts")
data class SosAlertEntity(
    @PrimaryKey val sosId: String,
    val senderName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val customMessage: String,
    val medicalNotes: String,
    val isEmergency: Boolean = true,
    val distanceMeters: Float = 0f,
    val status: String = "ACTIVE"
)

fun SosAlertEntity.toDomainModel(): SosAlert {
    return SosAlert(
        id = sosId,
        senderName = senderName,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        customMessage = customMessage,
        medicalNotes = medicalNotes,
        isEmergency = isEmergency,
        status = status
    )
}

fun SosAlert.toEntity(distanceMeters: Float = 0f): SosAlertEntity {
    return SosAlertEntity(
        sosId = id,
        senderName = senderName,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        customMessage = customMessage,
        medicalNotes = medicalNotes,
        isEmergency = isEmergency,
        distanceMeters = distanceMeters,
        status = status
    )
}
