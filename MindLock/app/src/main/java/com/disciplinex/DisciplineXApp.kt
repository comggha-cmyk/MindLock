package com.disciplinex

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DisciplineXApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(
                    getString(R.string.channel_alarm_id),
                    getString(R.string.channel_alarm_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarm notifications that wake you up"
                    setBypassDnd(true)
                    enableVibration(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    getString(R.string.channel_focus_id),
                    getString(R.string.channel_focus_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Active focus session indicator"
                }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    getString(R.string.channel_reminder_id),
                    getString(R.string.channel_reminder_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily reminders and streak notifications"
                }
            )
        }
    }
}
