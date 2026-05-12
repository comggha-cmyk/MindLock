package com.disciplinex.ui.components

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.disciplinex.ui.theme.DXColors
import com.disciplinex.utils.PushUpDetector
import com.google.accompanist.permissions.*

/**
 * Camera-based push-up challenge composable.
 * Uses ML Kit pose detection to count reps automatically.
 * Falls back to manual tap if camera permission is denied.
 */
@OptIn(ExperimentalPermissionsApi::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraPushUpChallenge(
    targetReps: Int = 10,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val detector = remember { PushUpDetector(context) }
    val repCount by detector.repCount.collectAsState()
    val feedback by detector.feedback.collectAsState()
    val currentState by detector.currentState.collectAsState()

    // Auto-complete when target reached
    LaunchedEffect(repCount) {
        if (repCount >= targetReps) {
            kotlinx.coroutines.delay(600)
            detector.release()
            onComplete()
        }
    }

    DisposableEffect(Unit) {
        onDispose { detector.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        when {
            cameraPermission.status.isGranted -> {
                // Camera preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { previewView ->
                            detector.startCamera(lifecycleOwner, previewView) {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                )

                // Overlay HUD
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: state indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DXColors.Background.copy(alpha = 0.7f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "POSE DETECTION ACTIVE",
                                style = MaterialTheme.typography.labelMedium,
                                color = DXColors.Secondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    when (currentState) {
                                        PushUpDetector.PushUpState.DOWN -> DXColors.Warning.copy(alpha = 0.8f)
                                        PushUpDetector.PushUpState.UP   -> DXColors.Secondary.copy(alpha = 0.8f)
                                        else                             -> DXColors.OnBackgroundFaint.copy(alpha = 0.8f)
                                    }
                                )
                                .size(12.dp)
                        )
                    }

                    // Bottom panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(DXColors.Surface)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rep counter
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$repCount",
                                style = MaterialTheme.typography.displayLarge,
                                color = DXColors.Secondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 72.sp
                            )
                            Column {
                                Text("/ $targetReps", style = MaterialTheme.typography.headlineMedium, color = DXColors.OnBackgroundMuted)
                                Text("REPS", style = MaterialTheme.typography.labelLarge, color = DXColors.OnBackgroundMuted, letterSpacing = 2.sp)
                            }
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { (repCount / targetReps.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = DXColors.Secondary,
                            trackColor = DXColors.SurfaceVariant
                        )

                        // Feedback text
                        Text(
                            feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DXColors.OnBackgroundMuted,
                            textAlign = TextAlign.Center
                        )

                        DXOutlinedButton(
                            text = "Cancel",
                            onClick = {
                                detector.release()
                                onCancel()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = DXColors.OnBackgroundMuted
                        )
                    }
                }
            }

            cameraPermission.status.shouldShowRationale ||
            !cameraPermission.status.isGranted -> {
                // Permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = DXColors.Primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Camera Required",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DXColors.OnBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "DisciplineX uses your camera to detect push-ups via ML Kit pose estimation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DXColors.OnBackgroundMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    DXButton(
                        text = "GRANT CAMERA",
                        onClick = { cameraPermission.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.CameraAlt
                    )
                    Spacer(Modifier.height(12.dp))
                    DXOutlinedButton(
                        text = "Manual Tap Instead",
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        color = DXColors.OnBackgroundMuted
                    )
                }
            }
        }
    }
}
