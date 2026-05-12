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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.disciplinex.data.entities.FocusSessionEntity
import com.disciplinex.goals.GoalsViewModel
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.DXColors

@Composable
fun GoalsScreen(vm: GoalsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        // Gold glow for achievement
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(DXColors.Warning.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.2f, 0f),
                    radius = 400f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionHeader("PROGRESS")
                    Text(
                        "Goals & Streaks",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DXColors.OnBackground,
                        fontWeight = FontWeight.Black
                    )
                }
                // Streak flame
                if (state.currentStreak > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥", fontSize = 32.sp)
                        Text(
                            "${state.currentStreak} day streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = DXColors.Warning
                        )
                    }
                }
            }

            // Achievement badges row
            if (state.badges.isNotEmpty()) {
                SectionHeader("ACHIEVEMENTS")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.badges) { badge ->
                        BadgeCard(badge)
                    }
                }
            }

            // Summary stats
            SectionHeader("YOUR NUMBERS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.EmojiEvents,
                    value = "${state.totalGoalsCompleted}",
                    label = "Completed",
                    color = DXColors.Warning,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${state.bestStreak}",
                    label = "Best Streak",
                    color = DXColors.Danger,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.Timer,
                    value = "${state.totalFocusHours}h",
                    label = "Total Focus",
                    color = DXColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.FitnessCenter,
                    value = "${state.totalPushUps}",
                    label = "Push-Ups",
                    color = DXColors.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Motivational quote card
            state.dailyQuote?.let { quote ->
                SectionHeader("TODAY'S FUEL")
                QuoteCard(quote)
            }

            // Completed sessions history
            SectionHeader("SESSION HISTORY")
            if (state.completedSessions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EmojiEvents,
                    message = "Complete your first focus session\nto start building your legacy.",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.completedSessions.forEach { session ->
                        SessionHistoryCard(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badge.color.copy(alpha = 0.12f))
            .border(1.dp, badge.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .width(80.dp)
    ) {
        Text(badge.emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            badge.label,
            style = MaterialTheme.typography.labelMedium,
            color = badge.color,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun QuoteCard(quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        DXColors.Warning.copy(alpha = 0.08f),
                        DXColors.SurfaceVariant
                    )
                )
            )
            .border(1.dp, DXColors.Warning.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⭐", fontSize = 20.sp)
            Text(
                "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge,
                color = DXColors.OnBackground,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun SessionHistoryCard(session: FocusSessionEntity) {
    NeonCard(glowColor = DXColors.Secondary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DXColors.Secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = DXColors.Secondary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text(
                        session.goalLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = DXColors.OnBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${session.durationMinutes} min · ${formatDate(session.completedAt ?: session.startedAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DXColors.OnBackgroundMuted
                    )
                }
            }
            Text(
                "${session.durationMinutes}m",
                style = MaterialTheme.typography.titleLarge,
                color = DXColors.Secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

data class Badge(
    val emoji: String,
    val label: String,
    val color: Color
)
