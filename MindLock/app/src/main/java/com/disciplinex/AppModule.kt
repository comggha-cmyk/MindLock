package com.disciplinex

import android.content.Context
import androidx.room.Room
import com.disciplinex.data.DisciplineXDatabase
import com.disciplinex.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DisciplineXDatabase {
        return Room.databaseBuilder(
            context,
            DisciplineXDatabase::class.java,
            "disciplinex.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideAlarmDao(db: DisciplineXDatabase): AlarmDao = db.alarmDao()
    @Provides fun provideBlockedAppDao(db: DisciplineXDatabase): BlockedAppDao = db.blockedAppDao()
    @Provides fun provideFocusSessionDao(db: DisciplineXDatabase): FocusSessionDao = db.focusSessionDao()
    @Provides fun provideDailyProgressDao(db: DisciplineXDatabase): DailyProgressDao = db.dailyProgressDao()
}
