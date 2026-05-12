package com.disciplinex.appblock

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinex.data.dao.BlockedAppDao
import com.disciplinex.data.entities.BlockedAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppBlockViewModel @Inject constructor(
    private val blockedAppDao: BlockedAppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val blockedApps: StateFlow<List<BlockedAppEntity>> = blockedAppDao.getAllBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val installedApps: StateFlow<List<Pair<String, String>>> = _installedApps.asStateFlow()

    val hasAccessibilityPermission: Boolean
        get() = isAccessibilityServiceEnabled()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { app ->
                    // Only user-installed + interesting system apps
                    (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
                    pm.getLaunchIntentForPackage(app.packageName) != null
                }
                .filter { it.packageName != context.packageName }
                .map { app ->
                    app.packageName to pm.getApplicationLabel(app).toString()
                }
                .sortedBy { it.second }

            _installedApps.value = apps
        }
    }

    fun addBlockedApp(app: Pair<String, String>) {
        viewModelScope.launch {
            blockedAppDao.insertBlockedApp(
                BlockedAppEntity(
                    packageName = app.first,
                    appName = app.second,
                    isBlocked = true
                )
            )
        }
    }

    fun toggleBlock(app: BlockedAppEntity) {
        viewModelScope.launch {
            blockedAppDao.setAppBlocked(app.packageName, !app.isBlocked)
        }
    }

    fun removeBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            blockedAppDao.deleteBlockedApp(app.packageName)
        }
    }

    fun updateWaitTimer(app: BlockedAppEntity, minutes: Int) {
        viewModelScope.launch {
            blockedAppDao.updateBlockedApp(app.copy(waitTimerMinutes = minutes))
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${context.packageName}/.services.AppBlockAccessibilityService"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
            .asSequence()
            .any { it.equals(service, ignoreCase = true) }
    }
}
