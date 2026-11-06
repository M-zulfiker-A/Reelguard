package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ReelGuardTab
import com.example.ui.ReelGuardViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuotesManagerScreen
import com.example.ui.screens.ReelTestLabScreen
import com.example.ui.screens.ThresholdsScreen
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbSurface
import com.example.ui.theme.AirbnbWhite
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ReelGuardApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelGuardApp(
    viewModel: ReelGuardViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val interventionNotice by viewModel.lastInterventionNotice.collectAsStateWithLifecycle()

    var isOverlayGranted by remember { mutableStateOf(viewModel.isOverlayPermissionGranted(context)) }
    var isAccessibilityGranted by remember { mutableStateOf(viewModel.isAccessibilityServiceEnabled(context)) }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.toggleFloatingBanner(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayGranted = viewModel.isOverlayPermissionGranted(context)
                isAccessibilityGranted = viewModel.isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (selectedTab != ReelGuardTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(ReelGuardTab.DASHBOARD)
        }
    }

    // Minimal Airbnb-style Intervention Modal
    if (interventionNotice != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInterventionNotice() },
            shape = RoundedCornerShape(24.dp),
            containerColor = AirbnbWhite,
            titleContentColor = AirbnbCharcoal,
            textContentColor = AirbnbSlate,
            title = {
                Text(
                    text = "Stepping out of the scroll",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AirbnbCharcoal
                )
            },
            text = {
                Column {
                    Text(
                        text = interventionNotice.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AirbnbSlate
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AirbnbSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"${sessionState.currentBannerMessage}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AirbnbCharcoal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissInterventionNotice()
                        viewModel.triggerBannerExit(exitToSystemHome = true, context = context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AirbnbRausch,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("modal_go_home_button")
                ) {
                    Text("Go to Home Screen")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.dismissInterventionNotice()
                        viewModel.resetSessionCounter()
                        viewModel.selectTab(ReelGuardTab.DASHBOARD)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("modal_reset_and_stay_button")
                ) {
                    Text(
                        text = "Reset & Stay",
                        color = AirbnbCharcoal
                    )
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = AirbnbWhite,
        topBar = {
            Column(modifier = Modifier.background(AirbnbWhite)) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AirbnbWhite,
                        titleContentColor = AirbnbCharcoal
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AirbnbRausch)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "reelguard",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = AirbnbCharcoal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Minimal Pill Status in Top Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AirbnbSurface)
                                    .border(1.dp, AirbnbDivider, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(sessionState.currentStage.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${sessionState.sessionReelsScrolled} reels · ${sessionState.bannerDelayMinutes}m delay",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AirbnbCharcoal,
                                    modifier = Modifier.testTag("topbar_reel_counter")
                                )
                            }
                        }
                    }
                )
                HorizontalDivider(color = AirbnbDivider)
            }
        },
        bottomBar = {
            Column(modifier = Modifier.background(AirbnbWhite)) {
                HorizontalDivider(color = AirbnbDivider)
                NavigationBar(
                    containerColor = AirbnbWhite,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == ReelGuardTab.DASHBOARD,
                        onClick = { viewModel.selectTab(ReelGuardTab.DASHBOARD) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = stringResource(R.string.nav_dashboard)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_dashboard)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AirbnbRausch,
                            selectedTextColor = AirbnbRausch,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = AirbnbSlate,
                            unselectedTextColor = AirbnbSlate
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ReelGuardTab.SIMULATOR,
                        onClick = { viewModel.selectTab(ReelGuardTab.SIMULATOR) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = stringResource(R.string.nav_simulator)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_simulator)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AirbnbRausch,
                            selectedTextColor = AirbnbRausch,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = AirbnbSlate,
                            unselectedTextColor = AirbnbSlate
                        ),
                        modifier = Modifier.testTag("nav_tab_simulator")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ReelGuardTab.MESSAGES,
                        onClick = { viewModel.selectTab(ReelGuardTab.MESSAGES) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.nav_quotes)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_quotes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AirbnbRausch,
                            selectedTextColor = AirbnbRausch,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = AirbnbSlate,
                            unselectedTextColor = AirbnbSlate
                        ),
                        modifier = Modifier.testTag("nav_tab_messages")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ReelGuardTab.THRESHOLDS,
                        onClick = { viewModel.selectTab(ReelGuardTab.THRESHOLDS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AirbnbRausch,
                            selectedTextColor = AirbnbRausch,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = AirbnbSlate,
                            unselectedTextColor = AirbnbSlate
                        ),
                        modifier = Modifier.testTag("nav_tab_thresholds")
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(220),
            label = "airbnbTabCrossfade"
        ) { tab ->
            when (tab) {
                ReelGuardTab.DASHBOARD -> {
                    DashboardScreen(
                        state = sessionState,
                        isOverlayGranted = isOverlayGranted,
                        isAccessibilityGranted = isAccessibilityGranted,
                        onToggleShield = { viewModel.toggleShield(it) },
                        onToggleFloatingOverlay = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !sessionState.isFloatingBannerActive) {
                                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.toggleFloatingBanner(context)
                            }
                        },
                        onQuickIncrementReel = {
                            viewModel.onReelScrolled("Quick Test")
                        },
                        onResetSession = { viewModel.resetSessionCounter() },
                        onOpenSimulator = { viewModel.selectTab(ReelGuardTab.SIMULATOR) },
                        onOpenDelaySettings = { viewModel.selectTab(ReelGuardTab.THRESHOLDS) },
                        onExitClicked = {
                            viewModel.triggerBannerExit(exitToSystemHome = false, context = context)
                        },
                        onShuffleQuote = { viewModel.shuffleBannerMessage() },
                        modifier = contentModifier
                    )
                }

                ReelGuardTab.SIMULATOR -> {
                    ReelTestLabScreen(
                        state = sessionState,
                        onSimulateReelSwipe = {
                            viewModel.onReelScrolled("Instagram Reels")
                        },
                        onTickOneSecond = {
                            viewModel.onSimulatorTickOneSecond()
                        },
                        onSkipDelayClicked = {
                            viewModel.skipBannerDelay()
                        },
                        onExitInstagramClicked = {
                            viewModel.triggerBannerExit(exitToSystemHome = false, context = context)
                        },
                        onShuffleQuote = { viewModel.shuffleBannerMessage() },
                        onResetCounter = { viewModel.resetSessionCounter() },
                        modifier = contentModifier
                    )
                }

                ReelGuardTab.MESSAGES -> {
                    QuotesManagerScreen(
                        state = sessionState,
                        messages = messages,
                        onAddMessage = { text, category, minStage ->
                            viewModel.addCustomMessage(text, category, minStage)
                        },
                        onToggleMessage = { viewModel.toggleMessageEnabled(it) },
                        onDeleteMessage = { viewModel.deleteMessage(it) },
                        onExitClicked = {
                            viewModel.triggerBannerExit(exitToSystemHome = false, context = context)
                        },
                        onShuffleQuote = { viewModel.shuffleBannerMessage() },
                        modifier = contentModifier
                    )
                }

                ReelGuardTab.THRESHOLDS -> {
                    ThresholdsScreen(
                        state = sessionState,
                        dailyLogs = dailyLogs,
                        onUpdateSettings = { delayMins, w, c, a, autoExit, haptics ->
                            viewModel.updateThresholds(delayMins, w, c, a, autoExit, haptics)
                        },
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}
