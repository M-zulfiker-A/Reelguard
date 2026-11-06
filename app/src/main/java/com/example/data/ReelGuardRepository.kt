package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReelSessionState(
    val isShieldEnabled: Boolean = true,
    val isFloatingBannerActive: Boolean = false,
    val isAccessibilityServiceConnected: Boolean = false,
    val isInstagramForeground: Boolean = false,
    val sessionReelsScrolled: Int = 0,
    val todayReelsScrolled: Int = 0,
    val todayHomeExits: Int = 0,
    val currentStage: BrainRotStage = BrainRotStage.MINDFUL,
    val currentBannerMessage: String = "Stay intentional with your time.",
    val bannerDelayMinutes: Int = 5,
    val instagramElapsedSeconds: Int = 0,
    val warningThreshold: Int = 5,
    val criticalThreshold: Int = 15,
    val autoExitThreshold: Int = 25,
    val autoExitOnLimit: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val recentScrollEvents: List<ScrollEventItem> = emptyList()
) {
    val bannerDelayTotalSeconds: Int
        get() = (bannerDelayMinutes.coerceAtLeast(1)) * 60

    val isBannerDelayElapsed: Boolean
        get() = instagramElapsedSeconds >= bannerDelayTotalSeconds

    val remainingDelaySeconds: Int
        get() = (bannerDelayTotalSeconds - instagramElapsedSeconds).coerceAtLeast(0)

    val formattedRemainingDelay: String
        get() {
            val mins = remainingDelaySeconds / 60
            val secs = remainingDelaySeconds % 60
            return String.format(Locale.US, "%d:%02d", mins, secs)
        }

    val formattedElapsedSessionTime: String
        get() {
            val mins = instagramElapsedSeconds / 60
            val secs = instagramElapsedSeconds % 60
            return String.format(Locale.US, "%d:%02d", mins, secs)
        }
}

class ReelGuardRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val dao = database.reelGuardDao()
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("reelguard_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow(loadInitialState())
    val sessionState: StateFlow<ReelSessionState> = _sessionState.asStateFlow()

    val allMessages: Flow<List<CustomMessageEntity>> = dao.getAllMessages()
    val recentDailyLogs: Flow<List<DailyScrollLogEntity>> = dao.getRecentDailyLogs()

    private var cachedEnabledMessages: List<CustomMessageEntity> = emptyList()
    private var requestExitToHomeCallback: (() -> Unit)? = null

    init {
        scope.launch {
            seedDefaultMessagesIfNeeded()
            refreshCachedMessages()
            syncTodayLogFromDb()
        }
        scope.launch {
            dao.getAllMessages().collect { list ->
                cachedEnabledMessages = list.filter { it.isEnabled }
                updateBannerMessageForStage(_sessionState.value.currentStage)
            }
        }
    }

    private fun loadInitialState(): ReelSessionState {
        val delayMins = prefs.getInt(KEY_BANNER_DELAY_MINUTES, 5).coerceIn(1, 60)
        val warning = prefs.getInt(KEY_WARNING_THRESHOLD, 5)
        val critical = prefs.getInt(KEY_CRITICAL_THRESHOLD, 15)
        val autoExit = prefs.getInt(KEY_AUTO_EXIT_THRESHOLD, 25)
        val autoExitEnabled = prefs.getBoolean(KEY_AUTO_EXIT_ENABLED, true)
        val shieldEnabled = prefs.getBoolean(KEY_SHIELD_ENABLED, true)
        val hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        return ReelSessionState(
            isShieldEnabled = shieldEnabled,
            bannerDelayMinutes = delayMins,
            warningThreshold = warning,
            criticalThreshold = critical,
            autoExitThreshold = autoExit,
            autoExitOnLimit = autoExitEnabled,
            hapticFeedbackEnabled = hapticsEnabled
        )
    }

    private suspend fun seedDefaultMessagesIfNeeded() {
        if (dao.getMessageCount() == 0) {
            val defaults = listOf(
                CustomMessageEntity(
                    messageText = "Stay intentional. What did you open Instagram to check?",
                    category = "Mindful",
                    minStageLevel = 1
                ),
                CustomMessageEntity(
                    messageText = "Another Reel? Your goals don't pause while you scroll.",
                    category = "Reality Check",
                    minStageLevel = 2
                ),
                CustomMessageEntity(
                    messageText = "You won't remember this video in 60 seconds.",
                    category = "Reality Check",
                    minStageLevel = 2
                ),
                CustomMessageEntity(
                    messageText = "Brain fog setting in. Step back for fresh air.",
                    category = "Wake Up",
                    minStageLevel = 3
                ),
                CustomMessageEntity(
                    messageText = "Is this Reel worth your focus right now?",
                    category = "Direct",
                    minStageLevel = 3
                ),
                CustomMessageEntity(
                    messageText = "Time to close the app and reclaim your day.",
                    category = "Direct",
                    minStageLevel = 4
                ),
                CustomMessageEntity(
                    messageText = "Limit reached. Tap Exit to step out of the scroll loop.",
                    category = "Urgent",
                    minStageLevel = 5
                )
            )
            dao.insertAllMessages(defaults)
        }
    }

    private suspend fun refreshCachedMessages() {
        cachedEnabledMessages = dao.getEnabledMessagesSnapshot()
    }

    private suspend fun syncTodayLogFromDb() {
        val today = getTodayDateKey()
        val existing = dao.getDailyLog(today)
        if (existing != null) {
            _sessionState.update { state ->
                val stage = BrainRotStage.fromReelsCount(
                    count = state.sessionReelsScrolled,
                    warningThreshold = state.warningThreshold,
                    criticalThreshold = state.criticalThreshold,
                    autoExitThreshold = state.autoExitThreshold
                )
                state.copy(
                    todayReelsScrolled = existing.reelsScrolled,
                    todayHomeExits = existing.homeExitsTriggered,
                    currentStage = stage
                )
            }
        } else {
            dao.upsertDailyLog(DailyScrollLogEntity(dateKey = today))
        }
    }

    fun registerExitHandler(handler: (() -> Unit)?) {
        requestExitToHomeCallback = handler
    }

    fun setAccessibilityServiceConnected(connected: Boolean) {
        _sessionState.update { it.copy(isAccessibilityServiceConnected = connected) }
    }

    fun setFloatingBannerActive(active: Boolean) {
        _sessionState.update { it.copy(isFloatingBannerActive = active) }
    }

    fun setInstagramForeground(inForeground: Boolean) {
        _sessionState.update { it.copy(isInstagramForeground = inForeground) }
    }

    fun setShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHIELD_ENABLED, enabled).apply()
        _sessionState.update { it.copy(isShieldEnabled = enabled) }
    }

    fun updateBannerDelayMinutes(minutes: Int) {
        val safeMins = minutes.coerceIn(1, 60)
        prefs.edit().putInt(KEY_BANNER_DELAY_MINUTES, safeMins).apply()
        _sessionState.update { it.copy(bannerDelayMinutes = safeMins) }
    }

    /**
     * Advances the active Instagram session timer and returns true once the configured
     * banner appearance delay (e.g. 5 minutes) has elapsed.
     */
    fun onInstagramSessionTick(secondsToAdd: Int = 1): Boolean {
        if (!_sessionState.value.isShieldEnabled) return false
        var delayNowMet = false
        _sessionState.update { state ->
            val updatedSeconds = (state.instagramElapsedSeconds + secondsToAdd).coerceAtLeast(0)
            val updatedState = state.copy(instagramElapsedSeconds = updatedSeconds)
            delayNowMet = updatedState.isBannerDelayElapsed
            updatedState
        }
        return delayNowMet
    }

    /**
     * Fast-forwards the active session timer past the configured delay so the user
     * can preview/test the banner appearance immediately.
     */
    fun skipBannerDelayForTesting() {
        _sessionState.update { state ->
            state.copy(instagramElapsedSeconds = state.bannerDelayTotalSeconds)
        }
    }

    fun recordReelScrolled(sourceLabel: String = "Instagram Reels"): Boolean {
        val current = _sessionState.value
        if (!current.isShieldEnabled) return false

        val newSessionCount = current.sessionReelsScrolled + 1
        val newTodayCount = current.todayReelsScrolled + 1
        // Each reel averages ~15s of viewing in simulation, while real Instagram also ticks 1s/sec
        val addedSeconds = if (sourceLabel == "Instagram App") 0 else 15
        val newElapsedSeconds = current.instagramElapsedSeconds + addedSeconds

        val newStage = BrainRotStage.fromReelsCount(
            count = newSessionCount,
            warningThreshold = current.warningThreshold,
            criticalThreshold = current.criticalThreshold,
            autoExitThreshold = current.autoExitThreshold
        )
        val pickedMessage = pickMessageForStage(newStage, newSessionCount)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val eventItem = ScrollEventItem(
            timestampLabel = timeFormat.format(Date()),
            sourceLabel = sourceLabel,
            reelNumber = newSessionCount,
            stage = newStage,
            bannerMessageShown = pickedMessage
        )

        val shouldAutoExit = current.autoExitOnLimit && newSessionCount >= current.autoExitThreshold

        _sessionState.update { state ->
            state.copy(
                sessionReelsScrolled = newSessionCount,
                todayReelsScrolled = newTodayCount,
                instagramElapsedSeconds = newElapsedSeconds,
                currentStage = newStage,
                currentBannerMessage = pickedMessage,
                recentScrollEvents = (listOf(eventItem) + state.recentScrollEvents).take(25)
            )
        }

        scope.launch {
            val today = getTodayDateKey()
            val existing = dao.getDailyLog(today)
            val peakLevel = maxOf(existing?.peakBrainRotLevel ?: 1, newStage.level)
            dao.upsertDailyLog(
                DailyScrollLogEntity(
                    dateKey = today,
                    reelsScrolled = newTodayCount,
                    homeExitsTriggered = _sessionState.value.todayHomeExits,
                    peakBrainRotLevel = peakLevel,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        if (shouldAutoExit) {
            triggerExitToHome()
        }
        return shouldAutoExit
    }

    fun triggerExitToHome() {
        val newExits = _sessionState.value.todayHomeExits + 1
        _sessionState.update { state ->
            state.copy(
                todayHomeExits = newExits,
                isInstagramForeground = false,
                instagramElapsedSeconds = 0
            )
        }
        scope.launch {
            val today = getTodayDateKey()
            val existing = dao.getDailyLog(today)
            dao.upsertDailyLog(
                DailyScrollLogEntity(
                    dateKey = today,
                    reelsScrolled = _sessionState.value.todayReelsScrolled,
                    homeExitsTriggered = newExits,
                    peakBrainRotLevel = existing?.peakBrainRotLevel ?: _sessionState.value.currentStage.level,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
        requestExitToHomeCallback?.invoke()
    }

    fun resetCurrentSessionCount() {
        _sessionState.update { state ->
            val freshStage = BrainRotStage.MINDFUL
            state.copy(
                sessionReelsScrolled = 0,
                instagramElapsedSeconds = 0,
                currentStage = freshStage,
                currentBannerMessage = pickMessageForStage(freshStage, 0)
            )
        }
    }

    fun cycleBannerMessage() {
        val state = _sessionState.value
        val nextMsg = pickMessageForStage(state.currentStage, state.sessionReelsScrolled + (1..10).random())
        _sessionState.update { it.copy(currentBannerMessage = nextMsg) }
    }

    private fun updateBannerMessageForStage(stage: BrainRotStage) {
        val msg = pickMessageForStage(stage, _sessionState.value.sessionReelsScrolled)
        _sessionState.update { it.copy(currentBannerMessage = msg) }
    }

    private fun pickMessageForStage(stage: BrainRotStage, seedIndex: Int): String {
        val eligible = cachedEnabledMessages.filter { it.minStageLevel <= stage.level }
        if (eligible.isEmpty()) {
            val fallback = cachedEnabledMessages
            if (fallback.isNotEmpty()) {
                return fallback[seedIndex % fallback.size].messageText
            }
            return "Take a breath and step out of the scroll loop."
        }
        val highestStageMatches = eligible.filter { it.minStageLevel == eligible.maxOf { m -> m.minStageLevel } }
        val pool = if (highestStageMatches.isNotEmpty()) highestStageMatches else eligible
        return pool[seedIndex.coerceAtLeast(0) % pool.size].messageText
    }

    fun updateThresholds(
        bannerDelayMinutes: Int,
        warning: Int,
        critical: Int,
        autoExit: Int,
        autoExitOnLimit: Boolean,
        hapticsEnabled: Boolean
    ) {
        val safeDelay = bannerDelayMinutes.coerceIn(1, 60)
        val safeWarning = warning.coerceIn(2, 50)
        val safeCritical = critical.coerceIn(safeWarning + 2, 100)
        val safeAutoExit = autoExit.coerceIn(safeCritical + 2, 200)

        prefs.edit()
            .putInt(KEY_BANNER_DELAY_MINUTES, safeDelay)
            .putInt(KEY_WARNING_THRESHOLD, safeWarning)
            .putInt(KEY_CRITICAL_THRESHOLD, safeCritical)
            .putInt(KEY_AUTO_EXIT_THRESHOLD, safeAutoExit)
            .putBoolean(KEY_AUTO_EXIT_ENABLED, autoExitOnLimit)
            .putBoolean(KEY_HAPTICS_ENABLED, hapticsEnabled)
            .apply()

        _sessionState.update { state ->
            val updatedStage = BrainRotStage.fromReelsCount(
                count = state.sessionReelsScrolled,
                warningThreshold = safeWarning,
                criticalThreshold = safeCritical,
                autoExitThreshold = safeAutoExit
            )
            state.copy(
                bannerDelayMinutes = safeDelay,
                warningThreshold = safeWarning,
                criticalThreshold = safeCritical,
                autoExitThreshold = safeAutoExit,
                autoExitOnLimit = autoExitOnLimit,
                hapticFeedbackEnabled = hapticsEnabled,
                currentStage = updatedStage,
                currentBannerMessage = pickMessageForStage(updatedStage, state.sessionReelsScrolled)
            )
        }
    }

    fun addCustomMessage(text: String, category: String, minStageLevel: Int) {
        if (text.isBlank()) return
        scope.launch {
            dao.insertMessage(
                CustomMessageEntity(
                    messageText = text.trim(),
                    category = category,
                    isEnabled = true,
                    minStageLevel = minStageLevel.coerceIn(1, 5)
                )
            )
        }
    }

    fun toggleMessageEnabled(message: CustomMessageEntity) {
        scope.launch {
            dao.updateMessage(message.copy(isEnabled = !message.isEnabled))
        }
    }

    fun deleteMessage(message: CustomMessageEntity) {
        scope.launch {
            dao.deleteMessage(message)
        }
    }

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    companion object {
        private const val KEY_BANNER_DELAY_MINUTES = "banner_delay_minutes"
        private const val KEY_WARNING_THRESHOLD = "warning_threshold"
        private const val KEY_CRITICAL_THRESHOLD = "critical_threshold"
        private const val KEY_AUTO_EXIT_THRESHOLD = "auto_exit_threshold"
        private const val KEY_AUTO_EXIT_ENABLED = "auto_exit_enabled"
        private const val KEY_SHIELD_ENABLED = "shield_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

        @Volatile
        private var INSTANCE: ReelGuardRepository? = null

        fun getInstance(context: Context): ReelGuardRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ReelGuardRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
