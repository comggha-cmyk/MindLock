package com.disciplinex.ui.components

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.disciplinex.ui.theme.DXColors
import kotlin.math.*

// ─────────────────────────────────────────────
// Neon Glow Card
// ─────────────────────────────────────────────
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowColor: Color = DXColors.Primary,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val m = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(DXColors.CardSurface)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(glowColor.copy(alpha = 0.5f), Color.Transparent, glowColor.copy(alpha = 0.2f))
            ),
            shape = RoundedCornerShape(16.dp)
        )
        .drawBehind {
            drawNeonGlow(glowColor.copy(alpha = 0.08f), 20.dp.toPx())
        }

    val finalMod = if (onClick != null) m.clickable { onClick() } else m

    Column(
        modifier = finalMod.padding(16.dp),
        content = content
    )
}

private fun DrawScope.drawNeonGlow(color: Color, radius: Float) {
    val paint = androidx.compose.ui.graphics.Paint().apply {
        asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(radius, 0f, 0f, android.graphics.Color.argb(
                (color.alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            ))
        }
    }
    drawContext.canvas.drawRoundRect(
        left = 0f, top = 0f, right = size.width, bottom = size.height,
        radiusX = 16.dp.toPx(), radiusY = 16.dp.toPx(), paint = paint
    )
}

// ─────────────────────────────────────────────
// Pulse Animated Icon
// ─────────────────────────────────────────────
@Composable
fun PulsingIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = DXColors.Primary,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint.copy(alpha = alpha),
        modifier = Modifier
            .size(size)
            .scale(scale)
    )
}

// ─────────────────────────────────────────────
// Circular Progress Timer
// ─────────────────────────────────────────────
@Composable
fun CircularTimer(
    progress: Float, // 0f to 1f
    timeText: String,
    label: String,
    color: Color = DXColors.Primary,
    size: Dp = 220.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "timer_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (this.size.minDimension - strokeWidth) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)

            // Background track
            drawCircle(
                color = DXColors.SurfaceVariant,
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(color.copy(alpha = 0.3f), color)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DXColors.OnBackgroundMuted
            )
        }
    }
}

// ─────────────────────────────────────────────
// Stat Chip
// ─────────────────────────────────────────────
@Composable
fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color = DXColors.Primary,
    modifier: Modifier = Modifier
) {
    NeonCard(modifier = modifier, glowColor = color) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, color = DXColors.OnBackground, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
            }
        }
    }
}

// ─────────────────────────────────────────────
// DX Primary Button
// ─────────────────────────────────────────────
@Composable
fun DXButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DXColors.Primary,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = DXColors.Background,
            disabledContainerColor = DXColors.SurfaceVariant,
            disabledContentColor = DXColors.OnBackgroundFaint
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// Outlined DX Button
// ─────────────────────────────────────────────
@Composable
fun DXOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DXColors.Primary,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = DXColors.OnBackgroundMuted,
        letterSpacing = 2.sp,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = DXColors.OnBackgroundFaint, modifier = Modifier.size(56.dp))
        Text(message, color = DXColors.OnBackgroundMuted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

val EaseInOutSine: Easing = Easing { fraction ->
    (-(cos(Math.PI * fraction) - 1) / 2).toFloat()
}
