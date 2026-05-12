package com.disciplinex.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────
// Alarm Entity
// ─────────────────────────────────────────────
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String = "Wake Up",
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // Comma-separated: "MON,TUE,WED"
    val dismissMethod: String = "PUSHUP", // PUSHUP or MANUAL
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Blocked App Entity
// ─────────────────────────────────────────────
@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val waitTimerMinutes: Int = 5, // Default 5 min wait
    val dismissMethod: String = "TIMER", // TIMER, PUSHUP, or BOTH
    val addedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Focus Session Entity
// ─────────────────────────────────────────────
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val durationMinutes: Int,
    val goalLabel: String = "Focus Session",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false,
    val motivationalQuote: String = ""
)

// ─────────────────────────────────────────────
// Daily Progress Entity
// ─────────────────────────────────────────────
@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey
    val date: String, // "YYYY-MM-DD"
    val goalsCompleted: Int = 0,
    val focusMinutes: Int = 0,
    val alarmsWokenTo: Int = 0,
    val pushUpsCompleted: Int = 0,
    val streakCount: Int = 0
)

// ─────────────────────────────────────────────
// Motivational Quotes (seeded)
// ─────────────────────────────────────────────
object MotivationalQuotes {
    val quotes = listOf(
        "Discipline is the bridge between goals and accomplishment.",
        "The secret of getting ahead is getting started.",
        "It does not matter how slowly you go as long as you do not stop.",
        "You don't have to be great to start, but you have to start to be great.",
        "Success is the sum of small efforts, repeated day in and day out.",
        "Don't watch the clock; do what it does. Keep going.",
        "The harder you work for something, the greater you'll feel when you achieve it.",
        "Dream bigger. Do bigger.",
        "Push yourself, because no one else is going to do it for you.",
        "Great things never come from comfort zones.",
        "Wake up with determination. Go to bed with satisfaction.",
        "The only bad workout is the one that didn't happen.",
        "Your future is created by what you do today, not tomorrow.",
        "Small daily improvements are the key to staggering long-term results.",
        "Every morning you have two choices: continue to sleep with your dreams, or wake up and chase them."
    )

    fun random() = quotes.random()
}
