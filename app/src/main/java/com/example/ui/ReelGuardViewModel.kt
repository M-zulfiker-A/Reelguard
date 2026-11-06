package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CustomMessageEntity
import com.example.data.DailyScrollLogEntity
import com.example.data.ReelGuardRepository
import com.example.data.ReelSessionState
import com.example.service.ReelBannerOverlayService
import com.example.service.ReelDetectorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

enum class ReelGuardTab {
    DASHBOARD,
    SIMULATOR,
    MESSAGES,
    THRESHOLDS
}

class ReelGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReelGuardRepository.getInstance(application)

    val sessionState: StateFlow<ReelSessionState> = repository.sessionState

    val allMessages: StateFlow<List<CustomMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLogs: StateFlow<List<DailyScrollLogEntity>> = repository.recentDailyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(ReelGuardTab.DASHBOARD)
    val selectedTab: StateFlow<ReelGuardTab> = _selectedTab.asStateFlow()

    private val _lastInterventionNotice = MutableStateFlow<String?>(null)
    val lastInterventionNotice: StateFlow<String?> = _lastInterventionNotice.asStateFlow()

    fun selectTab(tab: ReelGuardTab) {
        _selectedTab.value = tab
    }

    fun dismissInterventionNotice() {
        _lastInterventionNotice.value = null
    }

    fun toggleShield(enabled: Boolean) {
        repository.setShieldEnabled(enabled)
        if (!enabled && sessionState.value.isFloatingBannerActive) {
            ReelBannerOverlayService.stopBanner(getApplication())
        }
    }

    fun toggleFloatingBanner(context: Context) {
        if (sessionState.value.isFloatingBannerActive) {
            ReelBannerOverlayService.stopBanner(context)
        } else {
            // When manually previewing/pinning from Dashboard, mark delay as satisfied so it shows right away
            repository.skipBannerDelayForTesting()
            ReelBannerOverlayService.startBanner(context)
        }
    }

    fun onSimulatorTickOneSecond() {
        repository.onInstagramSessionTick(1)
    }

    fun skipBannerDelay() {
        repository.skipBannerDelayForTesting()
    }

    fun updateBannerDelayMinutes(minutes: Int) {
        repository.updateBannerDelayMinutes(minutes)
    }

    fun onReelScrolled(sourceLabel: String = "Reels Feed"): Boolean {
        val autoExited = repository.recordReelScrolled(sourceLabel)
        if (autoExited) {
            _lastInterventionNotice.value =
                "Reached your ${sessionState.value.autoExitThreshold}-reel limit. Stepping away from the feed."
        }
        return autoExited
    }

    fun triggerBannerExit(exitToSystemHome: Boolean, context: Context) {
        val scrolled = sessionState.value.sessionReelsScrolled
        val stageTitle = sessionState.value.currentStage.title
        repository.triggerExitToHome()
        _lastInterventionNotice.value =
            "Paused after $scrolled reels ($stageTitle). Your 5-minute grace timer has been reset for next time."
        if (exitToSystemHome) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(homeIntent)
            } catch (_: Exception) {}
        } else {
            _selectedTab.value = ReelGuardTab.DASHBOARD
        }
    }

    fun resetSessionCounter() {
        repository.resetCurrentSessionCount()
    }

    fun shuffleBannerMessage() {
        repository.cycleBannerMessage()
    }

    fun addCustomMessage(text: String, category: String, minStage: Int) {
        repository.addCustomMessage(text, category, minStage)
    }

    fun toggleMessageEnabled(message: CustomMessageEntity) {
        repository.toggleMessageEnabled(message)
    }

    fun deleteMessage(message: CustomMessageEntity) {
        repository.deleteMessage(message)
    }

    fun updateThresholds(
        bannerDelayMinutes: Int,
        warning: Int,
        critical: Int,
        autoExit: Int,
        autoExitOnLimit: Boolean,
        hapticsEnabled: Boolean
    ) {
        repository.updateThresholds(
            bannerDelayMinutes = bannerDelayMinutes,
            warning = warning,
            critical = critical,
            autoExit = autoExit,
            autoExitOnLimit = autoExitOnLimit,
            hapticsEnabled = hapticsEnabled
        )
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (sessionState.value.isAccessibilityServiceConnected) return true
        val expectedComponentName = "${context.packageName}/${ReelDetectorService::class.java.canonicalName}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                componentName.endsWith(ReelDetectorService::class.java.simpleName, ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }
}
