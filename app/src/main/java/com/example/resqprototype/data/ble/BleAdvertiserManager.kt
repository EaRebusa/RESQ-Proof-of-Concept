package com.example.resqprototype.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import com.example.resqprototype.domain.model.SosAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object BleConstants {
    val RESQ_SERVICE_UUID: ParcelUuid = ParcelUuid(UUID.fromString("0000FE50-0000-1000-8000-00805F9B34FB"))
    const val RESQ_MANUFACTURER_ID = 0x00E0 // Compact 2-byte Manufacturer ID

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk_gphone"))
    }
}

class BleAdvertiserManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = try {
        bluetoothManager?.adapter
    } catch (e: Exception) {
        null
    }

    private var advertiser: BluetoothLeAdvertiser? = null

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    fun isBleAdvertisingSupported(): Boolean {
        if (BleConstants.isEmulator()) return false

        return try {
            bluetoothAdapter != null &&
                    bluetoothAdapter.isEnabled &&
                    bluetoothAdapter.isMultipleAdvertisementSupported
        } catch (e: Exception) {
            false
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            _isAdvertising.value = true
        }

        override fun onStartFailure(errorCode: Int) {
            _isAdvertising.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(alert: SosAlert, batteryLevel: Int = 100): Boolean {
        if (!isBleAdvertisingSupported()) {
            _isAdvertising.value = true // Safe simulation mode fallback for emulator
            return false
        }

        return try {
            advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            if (advertiser == null) {
                _isAdvertising.value = true
                return false
            }

            val payload = SosPayloadCodec.encode(alert, batteryLevel)

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            // Compact 21-byte advertising data fits inside 31-byte legacy limit
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(BleConstants.RESQ_MANUFACTURER_ID, payload)
                .build()

            advertiser?.startAdvertising(settings, data, advertiseCallback)
            true
        } catch (e: Exception) {
            _isAdvertising.value = true
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            if (isBleAdvertisingSupported() && advertiser != null) {
                advertiser?.stopAdvertising(advertiseCallback)
            }
        } catch (e: Exception) {
            // Ignore
        }
        _isAdvertising.value = false
    }
}
