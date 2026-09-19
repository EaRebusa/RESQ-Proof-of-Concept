package com.example.resqprototype.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.io.File

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    initialFocusedAlertId: String? = null
) {
    val context = LocalContext.current
    val alerts by viewModel.alerts.collectAsState()
    val focusedAlert by viewModel.focusedAlert.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    // Initialize MapLibre engine once
    remember {
        MapLibre.getInstance(context)
    }

    val mapView = remember { MapView(context) }

    // Check if a local MBTiles file exists in app files directory
    val localMbtilesFile = remember { File(context.filesDir, "davao.mbtiles") }
    val styleUri = remember(localMbtilesFile.exists()) {
        if (localMbtilesFile.exists()) {
            "mbtiles://${localMbtilesFile.absolutePath}"
        } else {
            "https://tiles.openfreemap.org/styles/liberty"
        }
    }

    // If navigated from alert card with specific alert ID
    LaunchedEffect(initialFocusedAlertId, alerts) {
        if (!initialFocusedAlertId.isNullOrEmpty()) {
            val targetAlert = alerts.find { it.id == initialFocusedAlertId }
            if (targetAlert != null) {
                viewModel.focusAlert(targetAlert)
            }
        }
    }

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    mapInstance = map

                    // Enable full gesture controls
                    map.uiSettings.isZoomGesturesEnabled = true
                    map.uiSettings.isScrollGesturesEnabled = true
                    map.uiSettings.isRotateGesturesEnabled = true

                    map.setStyle(Style.Builder().fromUri(styleUri)) { _ ->
                        map.clear()

                        // Add markers for all active SOS alerts
                        alerts.forEach { alert ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(alert.latitude, alert.longitude))
                                    .title(alert.senderName)
                                    .snippet("🚨 ${alert.customMessage}")
                            )
                        }

                        // Animate camera to focused alert if present, otherwise default to Davao City
                        val targetLatLng = focusedAlert?.let {
                            LatLng(it.latitude, it.longitude)
                        } ?: userLocation?.let {
                            LatLng(it.latitude, it.longitude)
                        } ?: LatLng(7.0736, 125.6110)

                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(targetLatLng)
                                    .zoom(if (focusedAlert != null) 15.0 else 12.0)
                                    .build()
                            )
                        )
                    }
                }
            }
        )

        // Top Status Header Overlay Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (localMbtilesFile.exists()) "📍 Davao City Offline Map (MBTiles)" else "📍 Davao City Vector Map",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (focusedAlert != null)
                        "Focused on: ${focusedAlert?.senderName} (${focusedAlert?.customMessage})"
                    else
                        "Showing ${alerts.size} active emergency SOS markers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // On-screen Zoom (+ / -) & Recenter Controls for PC Emulator
        Column(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {
            SmallFloatingActionButton(
                onClick = { mapInstance?.animateCamera(CameraUpdateFactory.zoomIn()) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
            }

            Spacer(modifier = Modifier.height(8.dp))

            SmallFloatingActionButton(
                onClick = { mapInstance?.animateCamera(CameraUpdateFactory.zoomOut()) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = { viewModel.clearFocus() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Davao Overview", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
