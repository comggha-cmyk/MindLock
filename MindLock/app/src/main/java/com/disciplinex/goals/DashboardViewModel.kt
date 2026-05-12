package com.disciplinex.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinex.data.dao.DailyProgressDao
import com.disciplinex.data.dao.FocusSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DashboardState(
    val streakCount: Int = 0,
    val todayGoals: Int = 0,
    val focusMinutes: Int = 0,
    val pushUpsTotal: Int = 0,
    val blockedAttempts: Int = 0,
    val weekActivity: List<Boolean> = List(7) { false },
    val totalSessions: Int = 0,
    val totalFocusHours: Int = 0,
    val maxStreak: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val progressDao: DailyProgressDao,
    private val focusDao: FocusSessionDao
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val todayProgress = progressDao.getProgressForDate(today)
            val maxStreak = progressDao.getMaxStreak() ?: 0
            val totalSessions = focusDao.getTotalCompletedSessions()
            val totalFocusMinutes = focusDao.getTotalFocusMinutes() ?: 0

            // Build week activity (Mon-Sun of current week)
            val weekActivity = buildWeekActivity()

            _state.update {
                it.copy(
                    streakCount = todayProgress?.streakCount ?: 0,
                    todayGoals = todayProgress?.goalsCompleted ?: 0,
                    focusMinutes = todayProgress?.focusMinutes ?: 0,
                    pushUpsTotal = todayProgress?.pushUpsCompleted ?: 0,
                    weekActivity = weekActivity,
                    totalSessions = totalSessions,
                    totalFocusHours = totalFocusMinutes / 60,
                    maxStreak = maxStreak
                )
            }
        }
    }

    private suspend fun buildWeekActivity(): List<Boolean> {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        return (0..6).map { dayOffset ->
            val date = monday.plusDays(dayOffset.toLong()).format(fmt)
            val progress = progressDao.getProgressForDate(date)
            (progress?.goalsCompleted ?: 0) > 0 || (progress?.focusMinutes ?: 0) > 0
        }
    }

    fun refresh() = loadDashboard()
}
