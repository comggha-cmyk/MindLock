package com.disciplinex.alarm

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.disciplinex.R

// ─────────────────────────────────────────────
// Alarm Broadcast Receiver
// ─────────────────────────────────────────────
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val label = intent.getStringExtra("alarm_label") ?: "Alarm"

        // Start foreground service to handle alarm (plays sound, shows activity)
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_label", label)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Launch AlarmFiringActivity to show over lockscreen
        val activityIntent = Intent(context, AlarmFiringActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alarm_id", alarmId)
            putExtra("alarm_label", label)
        }
        context.startActivity(activityIntent)
    }
}

// ─────────────────────────────────────────────
// Boot Receiver – reschedule alarms
// ─────────────────────────────────────────────
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Note: In a full impl, reschedule all enabled alarms from Room DB here
            // Using a WorkManager task for reliable scheduling post-boot
        }
    }
}

// ─────────────────────────────────────────────
// Alarm Foreground Service
// ─────────────────────────────────────────────
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val ACTION_STOP = "com.disciplinex.STOP_ALARM"
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1
        val label = intent?.getStringExtra("alarm_label") ?: "Alarm"

        startForeground(alarmId + 1000, buildNotification(label))
        startSound()
        startVibration()

        return START_STICKY
    }

    private fun buildNotification(label: String): Notification {
        val fullscreenIntent = Intent(this, AlarmFiringActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val fullscreenPI = PendingIntent.getActivity(
            this, 0, fullscreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, getString(R.string.channel_alarm_id))
            .setContentTitle("⏰ $label")
            .setContentText("Tap to dismiss — complete your push-ups!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullscreenPI, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun startSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500, 500, 500, 1000, 200)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
