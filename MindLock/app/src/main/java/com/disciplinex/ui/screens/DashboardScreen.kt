package com.disciplinex.ui.screens

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
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.DXColors
import com.disciplinex.goals.DashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DXColors.Background, Color(0xFF0A0E18))
                )
            )
    ) {
        // Ambient glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(DXColors.Primary.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.1f),
                    radius = 400f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            DashboardHeader(state.streakCount)

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.CheckCircle,
                    value = "${state.todayGoals}",
                    label = "Goals Today",
                    color = DXColors.Secondary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Timer,
                    value = "${state.focusMinutes}m",
                    label = "Focused",
                    color = DXColors.Primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.FitnessCenter,
                    value = "${state.pushUpsTotal}",
                    label = "Push-Ups",
                    color = DXColors.Warning,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Block,
                    value = "${state.blockedAttempts}",
                    label = "Blocked",
                    color = DXColors.Danger,
                    modifier = Modifier.weight(1f)
                )
            }

            // Daily Motto
            DailyMottoCard()

            // Streak Calendar (last 7 days)
            SectionHeader("RECENT ACTIVITY")
            WeeklyStreak(state.weekActivity)

            // Total Stats
            SectionHeader("ALL TIME")
            NeonCard(glowColor = DXColors.Secondary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TotalStatItem("${state.totalSessions}", "Sessions", DXColors.Primary)
                    TotalStatItem("${state.totalFocusHours}h", "Focus Time", DXColors.Secondary)
                    TotalStatItem("${state.maxStreak}", "Best Streak", DXColors.Warning)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashboardHeader(streak: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DISCIPLINEX",
                style = MaterialTheme.typography.labelLarge,
                color = DXColors.Primary,
                letterSpacing = 3.sp
            )
            Text(
                text = greetingText(),
                style = MaterialTheme.typography.headlineMedium,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Black
            )
        }

        // Streak badge
        if (streak > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DXColors.Warning.copy(alpha = 0.15f))
                    .border(1.dp, DXColors.Warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 18.sp)
                    Column {
                        Text(
                            "$streak",
                            style = MaterialTheme.typography.titleLarge,
                            color = DXColors.Warning,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = DXColors.Warning.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMottoCard() {
    val motto = remember { com.disciplinex.data.entities.MotivationalQuotes.random() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(DXColors.PrimaryContainer, DXColors.SurfaceVariant)
                )
            )
            .border(
                1.dp,
                DXColors.Primary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "DAILY FUEL",
                style = MaterialTheme.typography.labelMedium,
                color = DXColors.Primary,
                letterSpacing = 2.sp
            )
            Text(
                "\"$motto\"",
                style = MaterialTheme.typography.bodyLarge,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun WeeklyStreak(weekActivity: List<Boolean>) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { i, day ->
            val active = weekActivity.getOrElse(i) { false }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) DXColors.Secondary.copy(alpha = 0.2f)
                            else DXColors.SurfaceVariant
                        )
                        .border(
                            1.dp,
                            if (active) DXColors.Secondary.copy(alpha = 0.6f) else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (active) {
                        Text("✓", color = DXColors.Secondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("·", color = DXColors.OnBackgroundFaint, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(day, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
            }
        }
    }
}

@Composable
private fun TotalStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
    }
}

private fun greetingText(): String {
    return when (java.time.LocalTime.now().hour) {
        in 5..11  -> "Good Morning."
        in 12..16 -> "Good Afternoon."
        in 17..20 -> "Good Evening."
        else      -> "Stay Strong."
    }
}
