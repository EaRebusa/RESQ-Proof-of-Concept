package com.example.resqprototype.data.ble

import com.example.resqprototype.domain.model.SosAlert
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Dual-Packet Binary Serializer for RESQ.
 * Packet A (21B) = Full Sender Name (Up to 15 characters).
 * Packet B (21B) = GPS Lat/Lng + Blood Type + Custom Emergency Note.
 *
 * Both packets stay strictly at 21 bytes (28 bytes total on air <= 31 byte BLE limit).
 */
object SosPayloadCodec {

    const val MAGIC_BYTE: Byte = 0x52 // ASCII 'R'
    const val PACKET_TYPE_A: Byte = 0x01 // Name Packet
    const val PACKET_TYPE_B: Byte = 0x02 // Location & Emergency Note Packet

    const val NAME_BYTES_SIZE = 15
    const val NOTE_BYTES_SIZE = 6
    const val PACKET_SIZE = 21

    private val bloodTypeMap = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

    fun encodePacketA(alert: SosAlert): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.BIG_ENDIAN)
        buffer.put(MAGIC_BYTE) // 1B
        buffer.putInt(alert.id.hashCode()) // 4B
        buffer.put(PACKET_TYPE_A) // 1B

        val nameBytes = alert.senderName.toByteArray(StandardCharsets.UTF_8)
        val fixedBytes = ByteArray(NAME_BYTES_SIZE)
        val bytesToCopy = Math.min(nameBytes.size, NAME_BYTES_SIZE)
        System.arraycopy(nameBytes, 0, fixedBytes, 0, bytesToCopy)
        buffer.put(fixedBytes) // 15B

        return buffer.array()
    }

    fun encodePacketB(alert: SosAlert): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.BIG_ENDIAN)
        buffer.put(MAGIC_BYTE) // 1B
        buffer.putInt(alert.id.hashCode()) // 4B
        buffer.put(PACKET_TYPE_B) // 1B

        buffer.putFloat(alert.latitude.toFloat()) // 4B
        buffer.putFloat(alert.longitude.toFloat()) // 4B

        val bloodIndex = bloodTypeMap.indexOfFirst {
            alert.medicalNotes.contains(it, ignoreCase = true)
        }.coerceAtLeast(0).toByte()
        buffer.put(bloodIndex) // 1B

        val noteBytes = alert.customMessage.toByteArray(StandardCharsets.UTF_8)
        val fixedNoteBytes = ByteArray(NOTE_BYTES_SIZE)
        val bytesToCopy = Math.min(noteBytes.size, NOTE_BYTES_SIZE)
        System.arraycopy(noteBytes, 0, fixedNoteBytes, 0, bytesToCopy)
        buffer.put(fixedNoteBytes) // 6B

        return buffer.array()
    }

    sealed class DecodedPacket {
        data class NamePacket(val sosHash: Int, val senderName: String) : DecodedPacket()
        data class LocationPacket(
            val sosHash: Int,
            val latitude: Double,
            val longitude: Double,
            val bloodType: String,
            val customNote: String
        ) : DecodedPacket()
    }

    fun decodePacket(bytes: ByteArray): DecodedPacket? {
        if (bytes.size < 6) return null

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val magic = buffer.get()
        if (magic != MAGIC_BYTE) return null

        val sosHash = buffer.int
        val packetType = buffer.get()

        return when (packetType) {
            PACKET_TYPE_A -> {
                if (bytes.size < PACKET_SIZE) return null
                val nameBuffer = ByteArray(NAME_BYTES_SIZE)
                buffer.get(nameBuffer)
                val nullIndex = nameBuffer.indexOf(0)
                val endLen = if (nullIndex >= 0) nullIndex else NAME_BYTES_SIZE
                val decodedName = String(nameBuffer, 0, endLen, StandardCharsets.UTF_8).trim()
                DecodedPacket.NamePacket(sosHash, decodedName.ifEmpty { "Nearby Rescuee" })
            }
            PACKET_TYPE_B -> {
                if (bytes.size < PACKET_SIZE) return null
                val lat = buffer.float.toDouble()
                val lng = buffer.float.toDouble()
                val bloodIdx = buffer.get().toInt().coerceIn(0, bloodTypeMap.lastIndex)
                val bloodType = bloodTypeMap[bloodIdx]

                val noteBuffer = ByteArray(NOTE_BYTES_SIZE)
                buffer.get(noteBuffer)
                val nullIndex = noteBuffer.indexOf(0)
                val endLen = if (nullIndex >= 0) nullIndex else NOTE_BYTES_SIZE
                val decodedNote = String(noteBuffer, 0, endLen, StandardCharsets.UTF_8).trim()

                DecodedPacket.LocationPacket(sosHash, lat, lng, bloodType, decodedNote)
            }
            else -> null
        }
    }
}
