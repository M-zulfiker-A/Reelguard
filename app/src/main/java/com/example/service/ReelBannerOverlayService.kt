package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.BrainRotStage
import com.example.data.ReelGuardRepository
import com.example.data.ReelSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class ReelBannerOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: ReelGuardRepository
    private var windowManager: WindowManager? = null
    private var overlayRootView: View? = null

    private var dotView: View? = null
    private var countTextView: TextView? = null
    private var messageTextView: TextView? = null
    private var exitButtonView: TextView? = null
    private var bannerBackground: GradientDrawable? = null
    private var lastKnownReelCount: Int = -1

    override fun onCreate() {
        super.onCreate()
        repository = ReelGuardRepository.getInstance(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopOverlayAndSelf()
                return START_NOT_STICKY
            }
            ACTION_EXIT_TO_HOME -> {
                performImmediateHomeExit()
                return START_STICKY
            }
        }

        val initialState = repository.sessionState.value
        val notification = buildForegroundNotification(initialState)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {}

        if (Settings.canDrawOverlays(this)) {
            showOrUpdateOverlayWindow()
        }
        repository.setFloatingBannerActive(true)

        serviceScope.launch {
            repository.sessionState.collectLatest { state ->
                updateOverlayViews(state)
                updateNotification(state)
                if (lastKnownReelCount != -1 && state.sessionReelsScrolled > lastKnownReelCount) {
                    if (state.hapticFeedbackEnabled) {
                        triggerHapticPulse(state.currentStage)
                    }
                }
                lastKnownReelCount = state.sessionReelsScrolled
            }
        }

        return START_STICKY
    }

    private fun showOrUpdateOverlayWindow() {
        if (overlayRootView != null) return

        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                resources.displayMetrics
            ).toInt()
        }

        // Compact, Minimal Airbnb-style Floating Pill (Single Horizontal Row)
        val pillContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(999).toFloat()
                setColor(Color.parseColor("#FCFFFFFF"))
                setStroke(dp(1), Color.parseColor("#E5E5E5"))
            }
            bannerBackground = bg
            background = bg
            setPadding(dp(12), dp(7), dp(8), dp(7))
            elevation = dp(10).toFloat()
        }

        // Status Dot
        val statusDot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                rightMargin = dp(7)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#008A05"))
            }
        }
        dotView = statusDot

        // Compact Brain Rot Counter Badge
        val countTv = TextView(this).apply {
            text = "0 reels"
            setTextColor(Color.parseColor("#222222"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(Typeface.DEFAULT_BOLD)
            setPadding(0, 0, dp(8), 0)
        }
        countTextView = countTv

        // Subtle Vertical Hairline Divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(1), dp(14)).apply {
                rightMargin = dp(8)
            }
            setBackgroundColor(Color.parseColor("#EBEBEB"))
        }

        // Minimal Configurable Message
        val messageTv = TextView(this).apply {
            text = "Stay intentional"
            setTextColor(Color.parseColor("#717171"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = dp(8)
            }
        }
        messageTextView = messageTv

        // Minimal Airbnb Rausch Coral "Exit" Pill
        val exitBtn = TextView(this).apply {
            text = "Exit"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
            setTypeface(Typeface.DEFAULT_BOLD)
            setPadding(dp(11), dp(5), dp(11), dp(5))
            background = GradientDrawable().apply {
                cornerRadius = dp(999).toFloat()
                setColor(Color.parseColor("#FF385C"))
            }
            setOnClickListener {
                performImmediateHomeExit()
            }
        }
        exitButtonView = exitBtn

        pillContainer.addView(statusDot)
        pillContainer.addView(countTv)
        pillContainer.addView(divider)
        pillContainer.addView(messageTv)
        pillContainer.addView(exitBtn)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(44)
        }

        var initialY = 0
        var initialTouchY = 0f
        var isDragging = false

        pillContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (abs(deltaY) > dp(8)) {
                        isDragging = true
                        params.y = (initialY + deltaY).coerceIn(dp(24), resources.displayMetrics.heightPixels - dp(120))
                        try {
                            windowManager?.updateViewLayout(pillContainer, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        v.performClick()
                        performImmediateHomeExit()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(pillContainer, params)
            overlayRootView = pillContainer
            updateOverlayViews(repository.sessionState.value)
        } catch (_: Exception) {}
    }

    private fun updateOverlayViews(state: ReelSessionState) {
        val stage = state.currentStage
        dotView?.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(stage.hexColor)
        }
        countTextView?.text = "${state.sessionReelsScrolled} • ${stage.shortTag}"
        messageTextView?.text = state.currentBannerMessage
    }

    private fun performImmediateHomeExit() {
        repository.triggerExitToHome()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(homeIntent)
        } catch (_: Exception) {}
        stopOverlayAndSelf()
    }

    private fun triggerHapticPulse(stage: BrainRotStage) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator()) return

            val duration = when (stage) {
                BrainRotStage.MINDFUL -> 18L
                BrainRotStage.DOPAMINE_SPIKE -> 30L
                BrainRotStage.BRAIN_FOG -> 50L
                BrainRotStage.SEVERE_ROT -> 90L
                BrainRotStage.COGNITIVE_MELTDOWN -> 160L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Minimal Brain Rot counter pill & Instagram exit action"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(state: ReelSessionState): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent = Intent(this, ReelBannerOverlayService::class.java).apply {
            action = ACTION_EXIT_TO_HOME
        }
        val exitPending = PendingIntent.getService(
            this,
            1,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ReelBannerOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPending = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stage = state.currentStage
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${state.sessionReelsScrolled} Reels • ${stage.shortTag}")
            .setContentText(state.currentBannerMessage)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit Instagram", exitPending)
            .addAction(android.R.drawable.ic_media_pause, "Hide", stopPending)
            .build()
    }

    private fun updateNotification(state: ReelSessionState) {
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.notify(NOTIFICATION_ID, buildForegroundNotification(state))
        } catch (_: Exception) {}
    }

    private fun stopOverlayAndSelf() {
        removeOverlayView()
        repository.setFloatingBannerActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeOverlayView() {
        overlayRootView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        overlayRootView = null
    }

    override fun onDestroy() {
        removeOverlayView()
        repository.setFloatingBannerActive(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "reelguard_banner_channel"
        private const val NOTIFICATION_ID = 4091
        const val ACTION_STOP_OVERLAY = "com.example.service.ACTION_STOP_OVERLAY"
        const val ACTION_EXIT_TO_HOME = "com.example.service.ACTION_EXIT_TO_HOME"

        fun startBanner(context: Context) {
            val intent = Intent(context, ReelBannerOverlayService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stopBanner(context: Context) {
            val intent = Intent(context, ReelBannerOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {
                context.stopService(Intent(context, ReelBannerOverlayService::class.java))
            }
        }
    }
}
