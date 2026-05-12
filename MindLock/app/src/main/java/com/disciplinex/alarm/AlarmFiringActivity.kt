package com.disciplinex.alarm

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class AlarmFiringActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lockscreen, turn on screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val keyguardManager = getSystemService(KeyguardManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }

        val alarmId = intent.getIntExtra("alarm_id", -1)
        val label = intent.getStringExtra("alarm_label") ?: "Alarm"

        setContent {
            DisciplineXTheme {
                AlarmFiringScreen(
                    alarmLabel = label,
                    onDismiss = { dismissAlarm() }
                )
            }
        }
    }

    private fun dismissAlarm() {
        val stopIntent = android.content.Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        startService(stopIntent)
        finish()
    }
}

@Composable
fun AlarmFiringScreen(
    alarmLabel: String,
    onDismiss: () -> Unit
) {
    var showPushUpChallenge by remember { mutableStateOf(false) }
    var pushUpCount by remember { mutableIntStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }

    // Auto dismiss after completing 10 push-ups
    LaunchedEffect(pushUpCount) {
        if (pushUpCount >= 10) {
            delay(500)
            dismissed = true
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        // Danger glow
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(DXColors.Danger.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 3),
                    radius = 500f
                )
            )
        }

        if (!showPushUpChallenge) {
            AlarmRingingView(
                label = alarmLabel,
                onStartChallenge = { showPushUpChallenge = true },
                onManualDismiss = onDismiss
            )
        } else {
            PushUpChallengeView(
                pushUpCount = pushUpCount,
                onPushUpCompleted = { pushUpCount++ },
                onManualComplete = onDismiss
            )
        }
    }
}

@Composable
private fun AlarmRingingView(
    label: String,
    onStartChallenge: () -> Unit,
    onManualDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_ring")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alarm_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(40.dp))

        // Time display
        Text(
            text = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            ),
            style = MaterialTheme.typography.displayLarge,
            color = DXColors.OnBackground,
            fontWeight = FontWeight.Black,
            fontSize = 80.sp
        )

        // Alarm icon pulsing
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(DXColors.Danger.copy(alpha = 0.15f))
                    .border(2.dp, DXColors.Danger.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = DXColors.Danger,
                    modifier = Modifier.size(60.dp)
                )
            }

            Text(
                label,
                style = MaterialTheme.typography.headlineMedium,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Complete 10 push-ups to dismiss",
                style = MaterialTheme.typography.bodyMedium,
                color = DXColors.OnBackgroundMuted,
                textAlign = TextAlign.Center
            )
        }

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DXButton(
                text = "DO PUSH-UPS 💪",
                onClick = onStartChallenge,
                modifier = Modifier.fillMaxWidth(),
                color = DXColors.Secondary,
                icon = Icons.Default.FitnessCenter
            )
            DXOutlinedButton(
                text = "Manual Dismiss (MVP)",
                onClick = onManualDismiss,
                modifier = Modifier.fillMaxWidth(),
                color = DXColors.OnBackgroundMuted
            )
        }
    }
}

@Composable
private fun PushUpChallengeView(
    pushUpCount: Int,
    onPushUpCompleted: () -> Unit,
    onManualComplete: () -> Unit
) {
    val progress = pushUpCount / 10f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Text(
                "PUSH-UP CHALLENGE",
                style = MaterialTheme.typography.labelLarge,
                color = DXColors.Primary,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Get on the floor and do it!",
                style = MaterialTheme.typography.bodyMedium,
                color = DXColors.OnBackgroundMuted
            )
        }

        // Progress Circle
        CircularTimer(
            progress = progress,
            timeText = "$pushUpCount",
            label = "of 10 push-ups",
            color = DXColors.Secondary,
            size = 240.dp
        )

        // Push-up rep buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pushUpCount < 10) {
                // MVP: Manual tap button for each rep
                DXButton(
                    text = "+ 1 PUSH-UP",
                    onClick = onPushUpCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    color = DXColors.Secondary,
                    icon = Icons.Default.Add
                )
                Text(
                    "Tap once per push-up\n(Camera detection coming soon)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DXColors.OnBackgroundMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                DXButton(
                    text = "✓ DONE! DISMISS ALARM",
                    onClick = onManualComplete,
                    modifier = Modifier.fillMaxWidth(),
                    color = DXColors.Secondary
                )
            }
        }
    }
}
