package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.example.data.ReelGuardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReelDetectorService : AccessibilityService() {

    private lateinit var repository: ReelGuardRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var instagramTimerJob: Job? = null
    private var exitCooldownJob: Job? = null
    private var lastScrollCountedAtMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = ReelGuardRepository.getInstance(applicationContext)
        repository.setAccessibilityServiceConnected(true)
        repository.registerExitHandler {
            stopInstagramTimerAndBanner(resetSession = true)
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        val state = repository.sessionState.value
        if (!state.isShieldEnabled) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val isInstagram = pkg.equals(INSTAGRAM_PACKAGE, ignoreCase = true)
                if (isInstagram) {
                    exitCooldownJob?.cancel()
                    repository.setInstagramForeground(true)
                    startInstagramSessionTimerIfNeeded()
                } else if (!isSystemOrOverlayPackage(pkg)) {
                    repository.setInstagramForeground(false)
                    // Give a 15-second grace window when briefly switching apps; if user stays out, reset session & hide banner
                    exitCooldownJob?.cancel()
                    exitCooldownJob = serviceScope.launch {
                        delay(15_000L)
                        if (!repository.sessionState.value.isInstagramForeground) {
                            stopInstagramTimerAndBanner(resetSession = true)
                        }
                    }
                }
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (pkg.equals(INSTAGRAM_PACKAGE, ignoreCase = true)) {
                    exitCooldownJob?.cancel()
                    repository.setInstagramForeground(true)
                    startInstagramSessionTimerIfNeeded()

                    val now = SystemClock.elapsedRealtime()
                    if (now - lastScrollCountedAtMs >= SCROLL_DEBOUNCE_MS) {
                        val className = event.className?.toString().orEmpty()
                        val isReelOrFeedPager = className.contains("ViewPager", ignoreCase = true) ||
                            className.contains("RecyclerView", ignoreCase = true) ||
                            className.contains("ScrollView", ignoreCase = true) ||
                            event.scrollDeltaY > 80 ||
                            event.fromIndex != event.toIndex

                        if (isReelOrFeedPager) {
                            lastScrollCountedAtMs = now
                            val shouldExit = repository.recordReelScrolled("Instagram App")
                            val latestState = repository.sessionState.value

                            // Only show floating banner AFTER the configured delay (default 5 mins) has elapsed
                            if (latestState.isBannerDelayElapsed &&
                                Settings.canDrawOverlays(this) &&
                                !latestState.isFloatingBannerActive
                            ) {
                                ReelBannerOverlayService.startBanner(this)
                            }

                            if (shouldExit) {
                                stopInstagramTimerAndBanner(resetSession = true)
                                performGlobalAction(GLOBAL_ACTION_HOME)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startInstagramSessionTimerIfNeeded() {
        if (instagramTimerJob?.isActive == true) return
        instagramTimerJob = serviceScope.launch {
            while (isActive && repository.sessionState.value.isInstagramForeground) {
                delay(1_000L)
                val delayElapsed = repository.onInstagramSessionTick(1)
                val current = repository.sessionState.value
                // Only trigger the banner after the user has been in Instagram for the configured delay (e.g. 5 mins)
                if (delayElapsed &&
                    current.isShieldEnabled &&
                    Settings.canDrawOverlays(this@ReelDetectorService) &&
                    !current.isFloatingBannerActive
                ) {
                    ReelBannerOverlayService.startBanner(this@ReelDetectorService)
                }
            }
        }
    }

    private fun stopInstagramTimerAndBanner(resetSession: Boolean) {
        instagramTimerJob?.cancel()
        instagramTimerJob = null
        if (repository.sessionState.value.isFloatingBannerActive) {
            ReelBannerOverlayService.stopBanner(this)
        }
        if (resetSession) {
            repository.resetCurrentSessionCount()
        }
    }

    private fun isSystemOrOverlayPackage(pkg: String): Boolean {
        return pkg == packageName ||
            pkg.startsWith("com.android.systemui") ||
            pkg.contains("inputmethod", ignoreCase = true) ||
            pkg.contains("keyboard", ignoreCase = true)
    }

    override fun onInterrupt() {
        if (::repository.isInitialized) {
            repository.setAccessibilityServiceConnected(false)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instagramTimerJob?.cancel()
        exitCooldownJob?.cancel()
        serviceScope.cancel()
        if (::repository.isInitialized) {
            repository.setAccessibilityServiceConnected(false)
            repository.registerExitHandler(null)
        }
        return super.onUnbind(intent)
    }

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val SCROLL_DEBOUNCE_MS = 1150L
    }
}
