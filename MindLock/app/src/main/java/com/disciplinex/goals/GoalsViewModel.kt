package com.disciplinex.goals

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinex.data.dao.DailyProgressDao
import com.disciplinex.data.dao.FocusSessionDao
import com.disciplinex.data.entities.FocusSessionEntity
import com.disciplinex.data.entities.MotivationalQuotes
import com.disciplinex.ui.screens.Badge
import com.disciplinex.ui.theme.DXColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalGoalsCompleted: Int = 0,
    val totalFocusHours: Int = 0,
    val totalPushUps: Int = 0,
    val completedSessions: List<FocusSessionEntity> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val dailyQuote: String? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val progressDao: DailyProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            focusSessionDao.getRecentCompletedSessions().collect { sessions ->
                _state.update { it.copy(completedSessions = sessions) }
            }
        }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val totalSessions = focusSessionDao.getTotalCompletedSessions()
            val totalFocusMinutes = focusSessionDao.getTotalFocusMinutes() ?: 0
            val maxStreak = progressDao.getMaxStreak() ?: 0
            val latest = progressDao.getLatestProgress()
            val allProgress = progressDao.getRecentProgress().first()
            val totalPushUps = allProgress.sumOf { it.pushUpsCompleted }

            val badges = buildBadges(totalSessions, totalFocusMinutes / 60, maxStreak, totalPushUps)

            _state.update {
                it.copy(
                    currentStreak = latest?.streakCount ?: 0,
                    bestStreak = maxStreak,
                    totalGoalsCompleted = totalSessions,
                    totalFocusHours = totalFocusMinutes / 60,
                    totalPushUps = totalPushUps,
                    badges = badges,
                    dailyQuote = MotivationalQuotes.random()
                )
            }
        }
    }

    private fun buildBadges(
        sessions: Int,
        focusHours: Int,
        streak: Int,
        pushUps: Int
    ): List<Badge> {
        val badges = mutableListOf<Badge>()

        if (sessions >= 1)   badges.add(Badge("🚀", "First Session", DXColors.Primary))
        if (sessions >= 10)  badges.add(Badge("💎", "10 Sessions", DXColors.Primary))
        if (sessions >= 50)  badges.add(Badge("👑", "50 Sessions", DXColors.Warning))
        if (streak >= 3)     badges.add(Badge("🔥", "3 Day Streak", DXColors.Danger))
        if (streak >= 7)     badges.add(Badge("⚡", "Week Streak", DXColors.Warning))
        if (streak >= 30)    badges.add(Badge("🏆", "Month Streak", DXColors.Warning))
        if (focusHours >= 10) badges.add(Badge("🎯", "10h Focus", DXColors.Secondary))
        if (pushUps >= 100)  badges.add(Badge("💪", "100 Push-Ups", DXColors.Secondary))
        if (pushUps >= 500)  badges.add(Badge("🦾", "500 Push-Ups", DXColors.Secondary))

        return badges
    }
}
