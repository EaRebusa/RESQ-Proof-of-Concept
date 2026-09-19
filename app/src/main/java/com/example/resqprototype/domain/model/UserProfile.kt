package com.example.resqprototype.domain.model

/**
 * Holds the local user's emergency profile used when broadcasting an SOS.
 */
data class UserProfile(
    val fullName: String = "Juan Dela Cruz",
    val phoneNumber: String = "+63 912 345 6789",
    val bloodType: String = "O+",
    val medicalConditions: String = "Asthma",
    val defaultNote: String = "EMERGENCY: Immediate assistance needed in Davao City area!"
)
