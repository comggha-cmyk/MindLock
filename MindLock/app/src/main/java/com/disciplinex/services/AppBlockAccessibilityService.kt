package com.disciplinex.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.disciplinex.appblock.AppBlockerActivity
import com.disciplinex.data.dao.BlockedAppDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockedAppDao: BlockedAppDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Debounce — don't fire repeatedly for same app
        if (packageName == lastBlockedPackage && System.currentTimeMillis() - lastBlockTime < 2000) return

        // Skip our own app and system UI
        if (packageName == applicationContext.packageName) return
        if (packageName == "com.android.systemui") return
        if (packageName == "com.android.launcher3") return

        scope.launch {
            val blockedApps = blockedAppDao.getActiveBlockedApps().first()
            val blocked = blockedApps.find { it.packageName == packageName && it.isBlocked }

            if (blocked != null) {
                lastBlockedPackage = packageName
                lastBlockTime = System.currentTimeMillis()

                val intent = Intent(applicationContext, AppBlockerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("blocked_package", packageName)
                    putExtra("app_name", blocked.appName)
                    putExtra("wait_minutes", blocked.waitTimerMinutes)
                }
                applicationContext.startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        scope.cancel()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
