package com.disciplinex.data.dao

import androidx.room.*
import com.disciplinex.data.entities.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// Alarm DAO
// ─────────────────────────────────────────────
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Int, enabled: Boolean)
}

// ─────────────────────────────────────────────
// Blocked Apps DAO
// ─────────────────────────────────────────────
@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Update
    suspend fun updateBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)

    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE packageName = :packageName")
    suspend fun setAppBlocked(packageName: String, blocked: Boolean)
}

// ─────────────────────────────────────────────
// Focus Session DAO
// ─────────────────────────────────────────────
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT 10")
    fun getRecentCompletedSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE isCompleted = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    suspend fun getTotalFocusMinutes(): Int?
}

// ─────────────────────────────────────────────
// Daily Progress DAO
// ─────────────────────────────────────────────
@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_progress ORDER BY date DESC")
    fun getAllProgress(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun getProgressForDate(date: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT 30")
    fun getRecentProgress(): Flow<List<DailyProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: DailyProgressEntity)

    @Query("SELECT MAX(streakCount) FROM daily_progress")
    suspend fun getMaxStreak(): Int?

    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT 1")
    suspend fun getLatestProgress(): DailyProgressEntity?
}
