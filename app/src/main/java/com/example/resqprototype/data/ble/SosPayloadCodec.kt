package com.example.resqprototype.data.ble

import com.example.resqprototype.domain.model.SosAlert
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Encodes and decodes SOS alert payloads into a compact 22-byte binary packet.
 * Guaranteed to fit inside standard BLE legacy advertising data limits (31 bytes max).
 */
object SosPayloadCodec {

    const val MAGIC_BYTE: Byte = 0x52 // ASCII 'R' for RESQ
    const val NAME_BYTES_SIZE = 5
    const val PACKET_SIZE = 22 // 17 bytes core + 5 bytes name = 22 bytes total (3B Flags + 4B Header + 22B = 29B <= 31B!)

    /**
     * Serializes a [SosAlert] into a 22-byte array.
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

        // Encode sender short name/initials into fixed 5-byte buffer
        val nameBytes = alert.senderName.toByteArray(StandardCharsets.UTF_8)
        val fixedNameBytes = ByteArray(NAME_BYTES_SIZE)
        val bytesToCopy = Math.min(nameBytes.size, NAME_BYTES_SIZE)
        System.arraycopy(nameBytes, 0, fixedNameBytes, 0, bytesToCopy)
        buffer.put(fixedNameBytes) // 5 bytes

        return buffer.array()
    }

    /**
     * Deserializes a 22-byte binary array back into a [SosAlert].
     */
    fun decode(bytes: ByteArray, defaultSenderName: String = "Nearby Rescuee"): SosAlert? {
        if (bytes.size < 17) return null // Must at least contain 17 core bytes

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

        var extractedName = defaultSenderName
        if (bytes.size >= PACKET_SIZE) {
            val nameBuffer = ByteArray(NAME_BYTES_SIZE)
            buffer.get(nameBuffer)
            val nullIndex = nameBuffer.indexOf(0)
            val endLen = if (nullIndex >= 0) nullIndex else NAME_BYTES_SIZE
            if (endLen > 0) {
                val decodedName = String(nameBuffer, 0, endLen, StandardCharsets.UTF_8).trim()
                if (decodedName.isNotEmpty()) {
                    extractedName = decodedName
                }
            }
        }

        return SosAlert(
            id = "sos_${Math.abs(sosHash)}",
            senderName = extractedName,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            customMessage = "Over-the-Air BLE Signal (Battery: $battery%)",
            medicalNotes = if (hasMedicalNote) "Medical Alert Active" else "",
            isEmergency = isEmergency,
            status = "ACTIVE"
        )
    }
}
