package com.disciplinex.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.disciplinex.focus.FocusViewModel
import com.disciplinex.focus.FocusState
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.DXColors

@Composable
fun FocusScreen(vm: FocusViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        // Cyan glow when active
        if (state.isActive) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(DXColors.Primary.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(size.width / 2, size.height / 3),
                        radius = 500f
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                SectionHeader("FOCUS MODE")
                Text(
                    if (state.isActive) "Stay in the zone." else "Deep Work",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DXColors.OnBackground,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(32.dp))

            if (!state.isActive) {
                FocusSetupView(
                    durationMinutes = state.selectedDurationMinutes,
                    onDurationChange = vm::setDuration,
                    goalLabel = state.goalLabel,
                    onGoalLabelChange = vm::setGoalLabel,
                    onStart = vm::startSession
                )
            } else {
                FocusActiveView(
                    state = state,
                    onStop = vm::stopSession,
                    onPause = vm::pauseSession
                )
            }

            Spacer(Modifier.height(24.dp))

            // Recent sessions
            if (state.recentSessions.isNotEmpty()) {
                SectionHeader("RECENT SESSIONS")
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.recentSessions.take(5)) { session ->
                        NeonCard(glowColor = if (session.isCompleted) DXColors.Secondary else DXColors.OnBackgroundFaint) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        null,
                                        tint = if (session.isCompleted) DXColors.Secondary else DXColors.OnBackgroundFaint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(session.goalLabel, style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackground)
                                        Text("${session.durationMinutes} minutes", style = MaterialTheme.typography.bodyMedium, color = DXColors.OnBackgroundMuted)
                                    }
                                }
                                if (session.isCompleted) {
                                    Text("✓", color = DXColors.Secondary, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusSetupView(
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    goalLabel: String,
    onGoalLabelChange: (String) -> Unit,
    onStart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Duration selector
        Text("Duration", style = MaterialTheme.typography.labelLarge, color = DXColors.OnBackgroundMuted)

        // Duration presets
        val presets = listOf(15, 25, 30, 45, 60, 90)
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            items(presets.size) { i ->
                val mins = presets[i]
                val selected = mins == durationMinutes
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) DXColors.PrimaryContainer else DXColors.SurfaceVariant
                        )
                        .border(
                            1.dp,
                            if (selected) DXColors.Primary.copy(alpha = 0.7f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onDurationChange(mins) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$mins",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) DXColors.Primary else DXColors.OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text("min", style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
                    }
                }
            }
        }

        OutlinedTextField(
            value = goalLabel,
            onValueChange = onGoalLabelChange,
            label = { Text("What will you work on?", color = DXColors.OnBackgroundMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DXColors.Primary,
                unfocusedBorderColor = DXColors.OnBackgroundFaint,
                focusedTextColor = DXColors.OnBackground,
                unfocusedTextColor = DXColors.OnBackground
            ),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Edit, null, tint = DXColors.OnBackgroundMuted) }
        )

        DXButton(
            text = "START FOCUS SESSION",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            color = DXColors.Primary,
            icon = Icons.Default.PlayArrow
        )
    }
}

@Composable
private fun FocusActiveView(
    state: FocusState,
    onStop: () -> Unit,
    onPause: () -> Unit
) {
    val progress = if (state.totalSeconds > 0) {
        1f - (state.secondsRemaining / state.totalSeconds.toFloat())
    } else 0f

    val timeStr = String.format("%02d:%02d", state.secondsRemaining / 60, state.secondsRemaining % 60)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            state.goalLabel,
            style = MaterialTheme.typography.titleLarge,
            color = DXColors.OnBackgroundMuted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        CircularTimer(
            progress = progress,
            timeText = timeStr,
            label = "remaining",
            color = DXColors.Primary,
            size = 260.dp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DXOutlinedButton(
                text = if (state.isPaused) "RESUME" else "PAUSE",
                onClick = onPause,
                modifier = Modifier.weight(1f),
                color = DXColors.Warning,
                icon = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
            )
            DXOutlinedButton(
                text = "STOP",
                onClick = onStop,
                modifier = Modifier.weight(1f),
                color = DXColors.Danger,
                icon = Icons.Default.Stop
            )
        }

        Text(
            "📵 Distracting apps are blocked",
            style = MaterialTheme.typography.bodyMedium,
            color = DXColors.Primary.copy(alpha = 0.7f)
        )
    }
}
