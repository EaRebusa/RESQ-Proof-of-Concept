package com.example.resqprototype.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.resqprototype.MainActivity
import com.example.resqprototype.R
import com.example.resqprototype.data.ble.BleScannerManager
import com.example.resqprototype.data.db.ResqDatabase
import com.example.resqprototype.data.repository.SosRepository
import com.example.resqprototype.domain.model.SosAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SosBleScannerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var scannerManager: BleScannerManager
    private lateinit var repository: SosRepository

    companion object {
        const val CHANNEL_ID = "resq_sos_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_SCANNER"
        const val ACTION_STOP = "ACTION_STOP_SCANNER"
    }

    override fun onCreate() {
        super.onCreate()
        scannerManager = BleScannerManager(this)
        repository = SosRepository(ResqDatabase.getInstance(this).sosAlertDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                try {
                    scannerManager.stopScanning()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (e: Exception) {
                    // Ignore foreground service stop exceptions
                }
                stopSelf()
            }
            else -> {
                try {
                    startForeground(NOTIFICATION_ID, buildForegroundNotification())
                } catch (e: Exception) {
                    // Safe fallback if Android 14+ FGS permissions are restricted
                }
                scannerManager.startScanning { alert ->
                    onAlertDiscovered(alert)
                }
            }
        }
        return START_STICKY
    }

    private fun onAlertDiscovered(alert: SosAlert) {
        serviceScope.launch {
            repository.saveAlert(alert)
            showEmergencyAlertNotification(alert)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RESQ Emergency Scanner Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when nearby offline SOS broadcasts are detected via Bluetooth LE"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RESQ Scanner Active")
            .setContentText("Scanning radio frequencies for offline emergency signals...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    private fun showEmergencyAlertNotification(alert: SosAlert) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🚨 EMERGENCY SOS DETECTED!")
                .setContentText("${alert.senderName}: ${alert.customMessage}")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(alert.id.hashCode(), notification)
        } catch (e: Exception) {
            // Ignore notification security exceptions
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            scannerManager.stopScanning()
        } catch (e: Exception) {
            // Ignore
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}
