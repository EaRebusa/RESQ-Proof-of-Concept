package com.example.resqprototype.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.example.resqprototype.domain.model.SosAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class BleScannerManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = try {
        bluetoothManager?.adapter
    } catch (e: Exception) {
        null
    }

    private var scanner: BluetoothLeScanner? = null

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var onAlertDiscoveredCallback: ((SosAlert) -> Unit)? = null

    // Dual-Packet Caches
    private val nameCache = ConcurrentHashMap<Int, String>()
    private val alertCache = ConcurrentHashMap<Int, SosAlert>()

    fun isBleScanningSupported(): Boolean {
        if (BleConstants.isEmulator()) return false

        return try {
            bluetoothAdapter != null && bluetoothAdapter.isEnabled
        } catch (e: Exception) {
            false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                val scanRecord = scanResult.scanRecord ?: return@let
                val rawPayload = scanRecord.getManufacturerSpecificData(BleConstants.RESQ_MANUFACTURER_ID)
                    ?: scanRecord.getServiceData(BleConstants.RESQ_SERVICE_UUID)
                    ?: return@let

                when (val decoded = SosPayloadCodec.decodePacket(rawPayload)) {
                    is SosPayloadCodec.DecodedPacket.NamePacket -> {
                        nameCache[decoded.sosHash] = decoded.senderName
                        val existingAlert = alertCache[decoded.sosHash]
                        if (existingAlert != null) {
                            val updatedAlert = existingAlert.copy(senderName = decoded.senderName)
                            alertCache[decoded.sosHash] = updatedAlert
                            onAlertDiscoveredCallback?.invoke(updatedAlert)
                        }
                    }
                    is SosPayloadCodec.DecodedPacket.LocationPacket -> {
                        val senderName = nameCache[decoded.sosHash] ?: try {
                            @SuppressLint("MissingPermission")
                            scanResult.device?.name ?: "Nearby Rescuee"
                        } catch (e: Exception) {
                            "Nearby Rescuee"
                        }

                        val alert = SosAlert(
                            id = "sos_${Math.abs(decoded.sosHash)}",
                            senderName = senderName,
                            latitude = decoded.latitude,
                            longitude = decoded.longitude,
                            timestamp = System.currentTimeMillis(),
                            customMessage = decoded.customNote,
                            medicalNotes = "Blood Type: ${decoded.bloodType}",
                            isEmergency = true,
                            status = "ACTIVE"
                        )
                        alertCache[decoded.sosHash] = alert
                        onAlertDiscoveredCallback?.invoke(alert)
                    }
                    null -> {
                        // Fallback for legacy single packets
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning(onAlertDiscovered: (SosAlert) -> Unit): Boolean {
        if (!isBleScanningSupported()) {
            _isScanning.value = true // Emulator simulation fallback
            return false
        }

        return try {
            scanner = bluetoothAdapter?.bluetoothLeScanner
            if (scanner == null) {
                _isScanning.value = true
                return false
            }

            this.onAlertDiscoveredCallback = onAlertDiscovered

            val filters = listOf(
                ScanFilter.Builder()
                    .setManufacturerData(BleConstants.RESQ_MANUFACTURER_ID, byteArrayOf())
                    .build()
            )

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanner?.startScan(filters, settings, scanCallback)
            _isScanning.value = true
            true
        } catch (e: Exception) {
            _isScanning.value = true
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (isBleScanningSupported() && scanner != null) {
                scanner?.stopScan(scanCallback)
            }
        } catch (e: Exception) {
            // Ignore
        }
        _isScanning.value = false
    }
}
