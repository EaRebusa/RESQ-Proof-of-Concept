package com.example.resqprototype.data.ble

import com.example.resqprototype.domain.model.SosAlert
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes and decodes SOS alert payloads into a compact 17-byte binary packet
 * designed to fit inside standard BLE advertising data limits (31 bytes max).
 */
object SosPayloadCodec {

    const val MAGIC_BYTE: Byte = 0x52 // ASCII 'R' for RESQ
    const val PACKET_SIZE = 17

    /**
     * Serializes a [SosAlert] into a 17-byte array.
     */
    fun encode(alert: SosAlert, batteryLevel: Int = 100): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.BIG_ENDIAN)

        buffer.put(MAGIC_BYTE) // 1 byte
        buffer.putInt(alert.id.hashCode()) // 4 bytes
        buffer.putFloat(alert.latitude.toFloat()) // 4 bytes
        buffer.putFloat(alert.longitude.toFloat()) // 4 bytes

        var flags: Byte = 0
        if (alert.isEmergency) flags = (flags.toInt() or 0x01).toByte()
        if (alert.medicalNotes.isNotEmpty()) flags = (flags.toInt() or 0x02).toByte()
        buffer.put(flags) // 1 byte

        val sequence = ((alert.timestamp / 1000) % 65535).toShort()
        buffer.putShort(sequence) // 2 bytes
        buffer.put(batteryLevel.coerceIn(0, 100).toByte()) // 1 byte

        return buffer.array()
    }

    /**
     * Deserializes a 17-byte binary array back into a [SosAlert].
     * Returns null if packet is invalid or magic byte does not match.
     */
    fun decode(bytes: ByteArray, defaultSenderName: String = "Unknown Broadcast"): SosAlert? {
        if (bytes.size < PACKET_SIZE) return null

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val magic = buffer.get()
        if (magic != MAGIC_BYTE) return null

        val sosHash = buffer.int
        val latitude = buffer.float.toDouble()
        val longitude = buffer.float.toDouble()
        val flags = buffer.get().toInt()
        val sequence = buffer.short
        val battery = buffer.get().toInt()

        val isEmergency = (flags and 0x01) != 0
        val hasMedicalNote = (flags and 0x02) != 0

        return SosAlert(
            id = "sos_${Math.abs(sosHash)}",
            senderName = defaultSenderName,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            customMessage = "BLE Beacon Packet (Battery: $battery%)",
            medicalNotes = if (hasMedicalNote) "Medical Alert Flag Active" else "",
            isEmergency = isEmergency
        )
    }
}
