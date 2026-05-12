package com.disciplinex.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.disciplinex.data.dao.BlockedAppDao
import com.disciplinex.focus.FocusService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var blockedAppDao: BlockedAppDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // Only suppress notifications during active focus sessions
        if (!FocusService.isRunning) return

        val packageName = sbn.packageName ?: return

        // Don't suppress our own notifications
        if (packageName == applicationContext.packageName) return

        scope.launch {
            val blockedApps = blockedAppDao.getActiveBlockedApps().first()
            val isBlocked = blockedApps.any { it.packageName == packageName && it.isBlocked }

            if (isBlocked) {
                try {
                    cancelNotification(sbn.key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onListenerDisconnected() {
        scope.cancel()
        super.onListenerDisconnected()
    }
}
