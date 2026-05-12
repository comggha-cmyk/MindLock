package com.disciplinex.focus

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.disciplinex.MainActivity
import com.disciplinex.R

class FocusService : Service() {

    companion object {
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        val durationMinutes = intent?.getIntExtra("duration_minutes", 25) ?: 25
        val goalLabel = intent?.getStringExtra("goal_label") ?: "Focus Session"

        val tapIntent = Intent(this, MainActivity::class.java)
        val tapPI = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.channel_focus_id))
            .setContentTitle("🎯 Focus Session Active")
            .setContentText("$goalLabel · ${durationMinutes}min — Stay locked in!")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(tapPI)
            .build()

        startForeground(9001, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
