package com.example.resqprototype.domain.model

import java.util.UUID

/**
 * Represents an Emergency SOS alert broadcasted or received via RESQ.
 *
 * @property id Unique identifier for the alert.
 * @property senderName Name of the person in distress.
 * @property latitude GPS Latitude coordinate.
 * @property longitude GPS Longitude coordinate.
 * @property timestamp Epoch timestamp (ms) when the alert was created.
 * @property customMessage Optional message describing the emergency situation.
 * @property medicalNotes Emergency medical details (e.g., Blood Type, Allergies).
 * @property isEmergency Active status of the alert.
 * @property status Incident lifecycle status ("ACTIVE", "RESOLVED", "ARCHIVED").
 */
data class SosAlert(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val customMessage: String = "",
    val medicalNotes: String = "",
    val isEmergency: Boolean = true,
    val status: String = "ACTIVE"
)
