package com.disciplinex.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.disciplinex.alarm.AlarmViewModel
import com.disciplinex.data.entities.AlarmEntity
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.DXColors
import java.util.Locale

@Composable
fun AlarmScreen(vm: AlarmViewModel = hiltViewModel()) {
    val alarms by vm.alarms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionHeader("SMART ALARM")
                    Text(
                        "Wake Up System",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DXColors.OnBackground,
                        fontWeight = FontWeight.Black
                    )
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = DXColors.Primary,
                    contentColor = DXColors.Background,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            }

            Spacer(Modifier.height(24.dp))

            if (alarms.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Alarm,
                    message = "No alarms set.\nTap + to create your first discipline alarm.",
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { vm.toggleAlarm(alarm) },
                            onDelete = { vm.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAlarmDialog(
                onDismiss = { showAddDialog = false },
                onSave = { alarm ->
                    vm.saveAlarm(alarm)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)

    NeonCard(
        glowColor = if (alarm.isEnabled) DXColors.Danger else DXColors.OnBackgroundFaint
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    timeStr,
                    style = MaterialTheme.typography.displayMedium,
                    color = if (alarm.isEnabled) DXColors.OnBackground else DXColors.OnBackgroundMuted,
                    fontWeight = FontWeight.Black
                )
                Text(
                    alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DXColors.OnBackgroundMuted
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = DXColors.Warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        if (alarm.dismissMethod == "PUSHUP") "Push-ups required" else "Manual dismiss",
                        style = MaterialTheme.typography.labelMedium,
                        color = DXColors.Warning
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DXColors.Background,
                        checkedTrackColor = DXColors.Danger,
                        uncheckedThumbColor = DXColors.OnBackgroundMuted,
                        uncheckedTrackColor = DXColors.SurfaceVariant
                    )
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DXColors.OnBackgroundFaint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("Wake Up") }
    var dismissMethod by remember { mutableStateOf("PUSHUP") }
    var vibrate by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DXColors.CardSurface,
        title = {
            Text(
                "NEW ALARM",
                style = MaterialTheme.typography.labelLarge,
                color = DXColors.Primary,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Time picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(
                        value = hour,
                        onValueChange = { hour = it.coerceIn(0, 23) },
                        label = "Hour",
                        range = 0..23
                    )
                    Text(":", style = MaterialTheme.typography.displayMedium, color = DXColors.OnBackground)
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it.coerceIn(0, 59) },
                        label = "Min",
                        range = 0..59
                    )
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label", color = DXColors.OnBackgroundMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DXColors.Primary,
                        unfocusedBorderColor = DXColors.OnBackgroundFaint,
                        focusedTextColor = DXColors.OnBackground,
                        unfocusedTextColor = DXColors.OnBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dismiss method
                Text("Dismiss Method", style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = dismissMethod == "PUSHUP",
                        onClick = { dismissMethod = "PUSHUP" },
                        label = { Text("Push-Ups 💪") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DXColors.PrimaryContainer,
                            selectedLabelColor = DXColors.Primary
                        )
                    )
                    FilterChip(
                        selected = dismissMethod == "MANUAL",
                        onClick = { dismissMethod = "MANUAL" },
                        label = { Text("Manual") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DXColors.PrimaryContainer,
                            selectedLabelColor = DXColors.Primary
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vibration", color = DXColors.OnBackgroundMuted, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = DXColors.Primary,
                            checkedThumbColor = DXColors.Background
                        )
                    )
                }
            }
        },
        confirmButton = {
            DXButton(
                text = "SET ALARM",
                onClick = {
                    onSave(
                        AlarmEntity(
                            hour = hour,
                            minute = minute,
                            label = label,
                            dismissMethod = dismissMethod,
                            vibrate = vibrate,
                            isEnabled = true
                        )
                    )
                },
                color = DXColors.Danger
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DXColors.OnBackgroundMuted)
            }
        }
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    range: IntRange
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onValueChange(if (value >= range.last) range.first else value + 1) }) {
            Icon(Icons.Default.KeyboardArrowUp, null, tint = DXColors.Primary)
        }
        Text(
            String.format(Locale.getDefault(), "%02d", value),
            style = MaterialTheme.typography.headlineLarge,
            color = DXColors.OnBackground,
            fontWeight = FontWeight.Black
        )
        IconButton(onClick = { onValueChange(if (value <= range.first) range.last else value - 1) }) {
            Icon(Icons.Default.KeyboardArrowDown, null, tint = DXColors.Primary)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
    }
}
