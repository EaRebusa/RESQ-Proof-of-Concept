package com.example.resqprototype.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqprototype.R
import com.example.resqprototype.domain.model.UserProfile
import com.example.resqprototype.ui.theme.CautionAmber
import com.example.resqprototype.ui.theme.EmergencyRed
import com.example.resqprototype.ui.theme.EmergencyRedDark
import com.example.resqprototype.ui.theme.SafetyGreen
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onSosTriggered: () -> Unit = {}
) {
    val context = LocalContext.current
    val isBroadcasting by viewModel.isBroadcasting.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val customNote by viewModel.customNote.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isHardwareBleSupported = viewModel.isHardwareBleSupported

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val buttonBgColor by animateColorAsState(
        targetValue = if (isBroadcasting) EmergencyRed else EmergencyRedDark,
        label = "SosButtonColor"
    )

    // Pulsing animation for active SOS state
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isBroadcasting) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val outerRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isBroadcasting) 1.3f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OuterRingScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // App Header with Custom Logo & Theme Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_resq_logo),
                    contentDescription = "RESQ Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "RESQ Sender Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Off-Grid Bluetooth LE Transmitter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Theme Switcher Button
            Box {
                IconButton(onClick = { showThemeMenu = true }) {
                    Icon(
                        imageVector = when (themeMode) {
                            "LIGHT" -> Icons.Default.LightMode
                            "DARK" -> Icons.Default.DarkMode
                            else -> Icons.Default.SettingsSuggest
                        },
                        contentDescription = "Theme Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("☀️ Light Mode") },
                        onClick = {
                            viewModel.updateThemeMode("LIGHT")
                            showThemeMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🌙 Dark Mode") },
                        onClick = {
                            viewModel.updateThemeMode("DARK")
                            showThemeMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⚙️ System Default") },
                        onClick = {
                            viewModel.updateThemeMode("SYSTEM")
                            showThemeMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Main Emergency SOS Button with Multi-Ring Pulsing
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Outer Pulsing Glow Ring
            if (isBroadcasting) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(outerRingScale)
                        .clip(CircleShape)
                        .background(EmergencyRed.copy(alpha = 0.2f))
                )
            }

            // Inner Button Body
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(buttonBgColor)
                    .border(
                        width = if (isBroadcasting) 5.dp else 2.dp,
                        color = if (isBroadcasting) Color.Yellow else Color.White.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .clickable {
                        // Prompt system Bluetooth enable intent if Bluetooth is OFF on physical device
                        try {
                            val btAdapter = BluetoothAdapter.getDefaultAdapter()
                            if (btAdapter != null && !btAdapter.isEnabled) {
                                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(enableBtIntent)
                            }
                        } catch (e: Exception) {
                            // Safe catch for permissions
                        }

                        viewModel.toggleSosBroadcast()
                        if (!isBroadcasting) {
                            onSosTriggered()
                        }
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS Icon",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBroadcasting) "CANCEL SOS" else "SEND SOS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Live Status Banner Card (High-Contrast for Light & Dark Modes)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBroadcasting) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isBroadcasting) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBroadcasting) "🔴 STATUS: BROADCASTING SOS SIGNAL" else "🟢 STATUS: STANDBY / READY",
                    fontWeight = FontWeight.Bold,
                    color = if (isBroadcasting) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hardware Status Badge Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isHardwareBleSupported) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isHardwareBleSupported)
                            "⚡ HARDWARE BLE RADIO ACTIVE"
                        else
                            "🛠️ SIMULATION FALLBACK (EMULATOR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isHardwareBleSupported) Color(0xFF1B5E20) else Color(0xFFE65100)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val formattedLat = String.format(Locale.US, "%.4f", currentLocation.latitude)
                val formattedLng = String.format(Locale.US, "%.4f", currentLocation.longitude)

                Text(
                    text = if (isBroadcasting)
                        "22-Byte radio beacon transmitting (GPS: $formattedLat, $formattedLng)."
                    else
                        "Live GPS: $formattedLat, $formattedLng • Press red button in emergency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBroadcasting) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Information Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Emergency Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Profile", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Sender: ${userProfile.fullName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Medical",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Blood Type: ${userProfile.bloodType} | Condition: ${userProfile.medicalConditions}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { viewModel.updateCustomNote(it) },
                    label = { Text("Custom Emergency Note") },
                    placeholder = { Text("Describe situation (e.g. Need boat, medical hazard)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    }

    // Edit Profile Modal Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.fullName) }
        var editPhone by remember { mutableStateOf(userProfile.phoneNumber) }
        var editBlood by remember { mutableStateOf(userProfile.bloodType) }
        var editConditions by remember { mutableStateOf(userProfile.medicalConditions) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Edit Emergency Profile", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name / Persona") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Emergency Contact Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBlood,
                        onValueChange = { editBlood = it },
                        label = { Text("Blood Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editConditions,
                        onValueChange = { editConditions = it },
                        label = { Text("Medical Conditions / Allergies") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newProfile = UserProfile(
                            fullName = editName,
                            phoneNumber = editPhone,
                            bloodType = editBlood,
                            medicalConditions = editConditions,
                            defaultNote = customNote
                        )
                        viewModel.updateFullProfile(newProfile)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
