package com.disciplinex.appblock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
class AppBlockerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: ""
        val appName = intent.getStringExtra("app_name") ?: "This App"
        val waitMinutes = intent.getIntExtra("wait_minutes", 5)

        setContent {
            DisciplineXTheme {
                AppBlockerScreen(
                    appName = appName,
                    waitMinutes = waitMinutes,
                    onAllowAccess = { finish() },
                    onGoBack = {
                        // Go to home screen instead of blocked app
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AppBlockerScreen(
    appName: String,
    waitMinutes: Int,
    onAllowAccess: () -> Unit,
    onGoBack: () -> Unit
) {
    var mode by remember { mutableStateOf<BlockMode>(BlockMode.Choosing) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        // Red danger glow
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(DXColors.Danger.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width / 2, size.height * 0.3f),
                    radius = 600f
                )
            )
        }

        when (mode) {
            BlockMode.Choosing -> ChoosingScreen(
                appName = appName,
                waitMinutes = waitMinutes,
                onChooseTimer = { mode = BlockMode.Timer(waitMinutes) },
                onChoosePushUps = { mode = BlockMode.PushUps },
                onGoBack = onGoBack
            )
            is BlockMode.Timer -> TimerScreen(
                minutes = (mode as BlockMode.Timer).minutes,
                appName = appName,
                onComplete = onAllowAccess,
                onCancel = { mode = BlockMode.Choosing }
            )
            BlockMode.PushUps -> PushUpBlockScreen(
                appName = appName,
                onComplete = onAllowAccess,
                onCancel = { mode = BlockMode.Choosing }
            )
        }
    }
}

sealed class BlockMode {
    object Choosing : BlockMode()
    data class Timer(val minutes: Int) : BlockMode()
    object PushUps : BlockMode()
}

@Composable
private fun ChoosingScreen(
    appName: String,
    waitMinutes: Int,
    onChooseTimer: () -> Unit,
    onChoosePushUps: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(40.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Block icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DXColors.Danger.copy(alpha = 0.15f))
                    .border(2.dp, DXColors.Danger.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Block, null, tint = DXColors.Danger, modifier = Modifier.size(50.dp))
            }

            Text(
                "BLOCKED",
                style = MaterialTheme.typography.labelLarge,
                color = DXColors.Danger,
                letterSpacing = 4.sp
            )
            Text(
                appName,
                style = MaterialTheme.typography.headlineLarge,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                "You blocked this app to stay focused.\nChoose how to earn access:",
                style = MaterialTheme.typography.bodyMedium,
                color = DXColors.OnBackgroundMuted,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Timer option
            NeonCard(
                glowColor = DXColors.Warning,
                onClick = onChooseTimer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DXColors.Warning.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Timer, null, tint = DXColors.Warning, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("Wait $waitMinutes Minutes", style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackground, fontWeight = FontWeight.Bold)
                        Text("Sit with your intention before entering", style = MaterialTheme.typography.bodyMedium, color = DXColors.OnBackgroundMuted)
                    }
                }
            }

            // Push-up option
            NeonCard(
                glowColor = DXColors.Secondary,
                onClick = onChoosePushUps,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DXColors.Secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, tint = DXColors.Secondary, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("Do 10 Push-Ups 💪", style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackground, fontWeight = FontWeight.Bold)
                        Text("Earn access through effort", style = MaterialTheme.typography.bodyMedium, color = DXColors.OnBackgroundMuted)
                    }
                }
            }

            DXOutlinedButton(
                text = "Go Back",
                onClick = onGoBack,
                modifier = Modifier.fillMaxWidth(),
                color = DXColors.OnBackgroundMuted,
                icon = Icons.Default.ArrowBack
            )
        }
    }
}

@Composable
private fun TimerScreen(
    minutes: Int,
    appName: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(minutes * 60) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onComplete()
    }

    val progress = secondsLeft / (minutes * 60f)
    val timeStr = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("COOLING OFF", style = MaterialTheme.typography.labelLarge, color = DXColors.Warning, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text("Wait to access $appName", style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackgroundMuted, textAlign = TextAlign.Center)
        }

        CircularTimer(
            progress = 1f - progress,
            timeText = timeStr,
            label = "remaining",
            color = DXColors.Warning,
            size = 240.dp
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Use this time to ask yourself:\nIs this really worth your focus?",
                style = MaterialTheme.typography.bodyMedium,
                color = DXColors.OnBackgroundMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            DXOutlinedButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                color = DXColors.OnBackgroundMuted
            )
        }
    }
}

@Composable
private fun PushUpBlockScreen(
    appName: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(count) {
        if (count >= 10) {
            delay(400)
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("EARN ACCESS", style = MaterialTheme.typography.labelLarge, color = DXColors.Secondary, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text("10 push-ups for $appName", style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackgroundMuted, textAlign = TextAlign.Center)
        }

        CircularTimer(
            progress = count / 10f,
            timeText = "$count",
            label = "of 10 reps",
            color = DXColors.Secondary,
            size = 220.dp
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (count < 10) {
                DXButton(
                    text = "+ 1 PUSH-UP",
                    onClick = { count++ },
                    modifier = Modifier.fillMaxWidth(),
                    color = DXColors.Secondary,
                    icon = Icons.Default.Add
                )
            }
            DXOutlinedButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                color = DXColors.OnBackgroundMuted
            )
        }
    }
}
