package com.disciplinex.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.disciplinex.data.dao.*
import com.disciplinex.data.entities.*

@Database(
    entities = [
        AlarmEntity::class,
        BlockedAppEntity::class,
        FocusSessionEntity::class,
        DailyProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DisciplineXDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyProgressDao(): DailyProgressDao
}
