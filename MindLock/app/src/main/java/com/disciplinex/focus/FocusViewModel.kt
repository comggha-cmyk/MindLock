package com.disciplinex.focus

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinex.data.dao.DailyProgressDao
import com.disciplinex.data.dao.FocusSessionDao
import com.disciplinex.data.entities.DailyProgressEntity
import com.disciplinex.data.entities.FocusSessionEntity
import com.disciplinex.data.entities.MotivationalQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class FocusState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val selectedDurationMinutes: Int = 25,
    val goalLabel: String = "Deep Work",
    val secondsRemaining: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val recentSessions: List<FocusSessionEntity> = emptyList(),
    val completionQuote: String = ""
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val progressDao: DailyProgressDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionId: Int = -1

    init {
        viewModelScope.launch {
            focusSessionDao.getRecentCompletedSessions().collect { sessions ->
                _state.update { it.copy(recentSessions = sessions) }
            }
        }
    }

    fun setDuration(minutes: Int) {
        _state.update { it.copy(
            selectedDurationMinutes = minutes,
            secondsRemaining = minutes * 60,
            totalSeconds = minutes * 60
        ) }
    }

    fun setGoalLabel(label: String) {
        _state.update { it.copy(goalLabel = label) }
    }

    fun startSession() {
        val duration = _state.value.selectedDurationMinutes
        val label = _state.value.goalLabel

        viewModelScope.launch {
            val session = FocusSessionEntity(
                durationMinutes = duration,
                goalLabel = label
            )
            val id = focusSessionDao.insertSession(session).toInt()
            currentSessionId = id

            _state.update { it.copy(
                isActive = true,
                isPaused = false,
                secondsRemaining = duration * 60,
                totalSeconds = duration * 60
            ) }

            startFocusService()
            startTimer()
        }
    }

    fun pauseSession() {
        if (_state.value.isPaused) {
            _state.update { it.copy(isPaused = false) }
            startTimer()
        } else {
            timerJob?.cancel()
            _state.update { it.copy(isPaused = true) }
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        stopFocusService()

        viewModelScope.launch {
            if (currentSessionId > 0) {
                val session = focusSessionDao.getAllSessions().first()
                    .find { it.id == currentSessionId }
                session?.let {
                    focusSessionDao.updateSession(it.copy(
                        isCompleted = false,
                        completedAt = System.currentTimeMillis()
                    ))
                }
            }
        }

        _state.update { it.copy(isActive = false, isPaused = false) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.secondsRemaining > 0 && _state.value.isActive && !_state.value.isPaused) {
                delay(1000)
                _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }

            if (_state.value.secondsRemaining == 0) {
                onSessionComplete()
            }
        }
    }

    private suspend fun onSessionComplete() {
        val state = _state.value
        val quote = MotivationalQuotes.random()

        // Mark session complete
        if (currentSessionId > 0) {
            val session = focusSessionDao.getAllSessions().first()
                .find { it.id == currentSessionId }
            session?.let {
                focusSessionDao.updateSession(it.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    motivationalQuote = quote
                ))
            }
        }

        // Update daily progress
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val existing = progressDao.getProgressForDate(today)
        val streakCount = calculateStreak(today)

        progressDao.insertOrUpdateProgress(
            DailyProgressEntity(
                date = today,
                goalsCompleted = (existing?.goalsCompleted ?: 0) + 1,
                focusMinutes = (existing?.focusMinutes ?: 0) + state.selectedDurationMinutes,
                alarmsWokenTo = existing?.alarmsWokenTo ?: 0,
                pushUpsCompleted = existing?.pushUpsCompleted ?: 0,
                streakCount = streakCount
            )
        )

        stopFocusService()
        _state.update { it.copy(isActive = false, isPaused = false, completionQuote = quote) }
    }

    private suspend fun calculateStreak(today: String): Int {
        val recent = progressDao.getRecentProgress().first()
        var streak = 1
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        var checkDate = LocalDate.parse(today, fmt).minusDays(1)

        for (i in 1..30) {
            val dateStr = checkDate.format(fmt)
            val progress = recent.find { it.date == dateStr }
            if (progress != null && (progress.goalsCompleted > 0 || progress.focusMinutes > 0)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else break
        }
        return streak
    }

    private fun startFocusService() {
        val intent = Intent(context, FocusService::class.java).apply {
            putExtra("duration_minutes", _state.value.selectedDurationMinutes)
            putExtra("goal_label", _state.value.goalLabel)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopFocusService() {
        context.stopService(Intent(context, FocusService::class.java))
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
