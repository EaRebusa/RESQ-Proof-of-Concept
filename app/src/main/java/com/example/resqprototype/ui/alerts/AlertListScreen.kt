package com.example.resqprototype.ui.alerts

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqprototype.data.location.DistanceCalculator
import com.example.resqprototype.domain.model.SosAlert
import com.example.resqprototype.ui.theme.CautionAmber
import com.example.resqprototype.ui.theme.EmergencyRed
import com.example.resqprototype.ui.theme.SafetyGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertListScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertViewModel = viewModel(),
    onViewMapClicked: (SosAlert) -> Unit = {}
) {
    val alerts by viewModel.alerts.collectAsState()
    val rescuerLocation by viewModel.rescuerLocation.collectAsState()
    val isHardwareBleScannerSupported = viewModel.isHardwareBleScannerSupported
    var selectedAlert by remember { mutableStateOf<SosAlert?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header & Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Rescuer Alert Feed",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Nearby Offline SOS Radio Signals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Hardware Scanner Status Badge Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isHardwareBleScannerSupported) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isHardwareBleScannerSupported)
                            "⚡ HARDWARE SCANNER ACTIVE"
                        else
                            "🛠️ SIMULATION SCANNER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isHardwareBleScannerSupported) SafetyGreen else CautionAmber
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Simulate incoming SOS button (only shown on PC emulators)
                if (!isHardwareBleScannerSupported) {
                    Button(
                        onClick = {
                            viewModel.addMockAlert(
                                name = "Distress Citizen ${alerts.size + 1}",
                                note = "Urgent: Trapped near Matina Bridge area."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Add Mock Alert",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (alerts.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.archiveAllAlerts() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Archive All", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No active distress signals detected nearby.\nScanning radio frequencies...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        rescuerLocation = rescuerLocation,
                        onCardClicked = { selectedAlert = alert },
                        onViewMapClicked = { onViewMapClicked(alert) },
                        onMarkResolved = { viewModel.markAlertResolved(alert.id) },
                        onArchiveClicked = { viewModel.archiveAlert(alert.id) }
                    )
                }
            }
        }
    }

    // Alert Detail Dialog Modal
    selectedAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlert = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (alert.status == "RESOLVED") SafetyGreen else EmergencyRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (alert.status == "RESOLVED") "Resolved Incident" else "Active SOS Alert Details",
                        fontWeight = FontWeight.Bold,
                        color = if (alert.status == "RESOLVED") SafetyGreen else EmergencyRed
                    )
                }
            },
            text = {
                Column {
                    Text("Sender: ${alert.senderName}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("GPS Coordinates: ${alert.latitude}, ${alert.longitude}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Broadcast Time: ${formatTime(alert.timestamp)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Medical Details:", fontWeight = FontWeight.Bold)
                    Text(alert.medicalNotes.ifEmpty { "None specified" }, color = EmergencyRed)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Emergency Situation Note:", fontWeight = FontWeight.Bold)
                    Text(alert.customMessage.ifEmpty { "No custom note provided." })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onViewMapClicked(alert)
                        selectedAlert = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View on Offline Map")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAlert = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AlertCard(
    alert: SosAlert,
    rescuerLocation: Location,
    onCardClicked: () -> Unit,
    onViewMapClicked: () -> Unit,
    onMarkResolved: () -> Unit,
    onArchiveClicked: () -> Unit
) {
    val isResolved = alert.status == "RESOLVED"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClicked() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Status Badge Tag & Archive Trash Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isResolved) SafetyGreen else EmergencyRed
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isResolved) "✅ RESCUED / RESOLVED" else "🚨 ACTIVE DISTRESS SIGNAL",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTime(alert.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onArchiveClicked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Archive Alert Log",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sender Person Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isResolved) SafetyGreen else EmergencyRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = alert.senderName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Proximity & Location Badge Row (Dynamic Live Location Calculation!)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val distMeters = DistanceCalculator.calculateDistanceMeters(
                    rescuerLocation.latitude, rescuerLocation.longitude,
                    alert.latitude, alert.longitude
                )
                val formattedDist = DistanceCalculator.formatDistance(distMeters)
                Text(
                    text = "GPS: ${String.format(Locale.US, "%.4f", alert.latitude)}, ${String.format(Locale.US, "%.4f", alert.longitude)} • $formattedDist away",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (alert.customMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = alert.customMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (alert.medicalNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = EmergencyRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.medicalNotes,
                        style = MaterialTheme.typography.labelMedium,
                        color = EmergencyRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: View on Map & Mark Rescued
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewMapClicked,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Map", fontSize = 12.sp)
                }

                if (!isResolved) {
                    Button(
                        onClick = onMarkResolved,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Rescued", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
