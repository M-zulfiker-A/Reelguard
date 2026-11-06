package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BrainRotStage
import com.example.data.ReelSessionState
import com.example.ui.components.PersistentBrainRotBanner
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbSurface
import com.example.ui.theme.AirbnbWhite
import com.example.ui.theme.StageFreshGreen

@Composable
fun DashboardScreen(
    state: ReelSessionState,
    isOverlayGranted: Boolean,
    isAccessibilityGranted: Boolean,
    onToggleShield: (Boolean) -> Unit,
    onToggleFloatingOverlay: () -> Unit,
    onQuickIncrementReel: () -> Unit,
    onResetSession: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenDelaySettings: () -> Unit,
    onExitClicked: () -> Unit,
    onShuffleQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AirbnbWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Minimal Banner Preview Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minimal Banner Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                    Text(
                        text = if (state.isBannerDelayElapsed) {
                            "Active (${state.bannerDelayMinutes}m reached)"
                        } else {
                            "Appears after ${state.bannerDelayMinutes}m on Instagram"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isBannerDelayElapsed) StageFreshGreen else AirbnbSlate
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                PersistentBrainRotBanner(
                    state = state,
                    onExitClicked = onExitClicked,
                    onShuffleQuote = onShuffleQuote
                )
            }
        }

        // Primary Airbnb-Style Status & Delay Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AirbnbWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isShieldEnabled) "Protection active" else "Protection paused",
                                style = MaterialTheme.typography.headlineMedium,
                                color = AirbnbCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Waits ${state.bannerDelayMinutes} min after opening Instagram before showing the top banner.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AirbnbSlate
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = state.isShieldEnabled,
                            onCheckedChange = onToggleShield,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AirbnbCharcoal,
                                uncheckedThumbColor = AirbnbSlate,
                                uncheckedTrackColor = AirbnbSurface
                            ),
                            modifier = Modifier.testTag("master_shield_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = AirbnbDivider)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Configurable 5-Minute Delay Pill Row (Tappable like Airbnb date/guest selector)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AirbnbSurface)
                            .clickable { onOpenDelaySettings() }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("delay_summary_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AirbnbRausch,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Banner delay: ${state.bannerDelayMinutes} minutes",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AirbnbCharcoal,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (state.isBannerDelayElapsed) {
                                        "Grace window finished • Banner visible"
                                    } else {
                                        "${state.formattedRemainingDelay} remaining in grace window"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AirbnbSlate
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Edit delay",
                            tint = AirbnbSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenSimulator,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AirbnbRausch,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_simulator_button")
                        ) {
                            Text(
                                text = "Try Reels Lab",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onToggleFloatingOverlay,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("toggle_floating_banner_button")
                        ) {
                            Text(
                                text = if (state.isFloatingBannerActive) "Hide Overlay" else "Preview Overlay",
                                style = MaterialTheme.typography.labelLarge,
                                color = AirbnbCharcoal
                            )
                        }
                    }
                }
            }
        }

        // Minimal Brain Rot Counter & Stage Card
        item {
            BrainRotMinimalCard(
                state = state,
                onQuickIncrementReel = onQuickIncrementReel,
                onResetSession = onResetSession
            )
        }

        // Clean 2-Column Editorial Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinimalStatTile(
                    label = "Today's reels",
                    value = "${state.todayReelsScrolled}",
                    footnote = "~${(state.todayReelsScrolled * 0.45).toInt()} min spent",
                    modifier = Modifier.weight(1f)
                )
                MinimalStatTile(
                    label = "Mindful exits",
                    value = "${state.todayHomeExits}",
                    footnote = "~${state.todayHomeExits * 12} min saved",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // System Permissions Section (Clean Airbnb list rows)
        item {
            MinimalPermissionsSection(
                context = context,
                isOverlayGranted = isOverlayGranted,
                isAccessibilityGranted = isAccessibilityGranted
            )
        }

        // Recent Activity
        if (state.recentScrollEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleLarge,
                    color = AirbnbCharcoal
                )
            }
            items(state.recentScrollEvents.take(5), key = { it.id }) { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(event.stage.color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reel #${event.reelNumber} · ${event.stage.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AirbnbCharcoal
                            )
                            Text(
                                text = event.bannerMessageShown,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AirbnbSlate,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = event.timestampLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                }
                HorizontalDivider(color = AirbnbDivider, modifier = Modifier.padding(top = 8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BrainRotMinimalCard(
    state: ReelSessionState,
    onQuickIncrementReel: () -> Unit,
    onResetSession: () -> Unit
) {
    val stage = state.currentStage

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AirbnbWhite,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Brain rot counter",
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AirbnbCharcoal,
                        modifier = Modifier.testTag("current_stage_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AirbnbSlate
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${state.sessionReelsScrolled}",
                        style = MaterialTheme.typography.displayLarge,
                        color = AirbnbCharcoal,
                        modifier = Modifier.testTag("dashboard_session_reels_count")
                    )
                    Text(
                        text = "of ${state.autoExitThreshold} limit",
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimal 5-segment progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BrainRotStage.entries.forEach { entry ->
                    val active = state.currentStage.level >= entry.level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(if (active) entry.color else AirbnbSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickIncrementReel,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("quick_increment_reel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AirbnbCharcoal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Count +1 reel",
                        style = MaterialTheme.typography.labelLarge,
                        color = AirbnbCharcoal
                    )
                }

                OutlinedButton(
                    onClick = onResetSession,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("reset_session_button")
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = AirbnbSlate
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalStatTile(
    label: String,
    value: String,
    footnote: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AirbnbSurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AirbnbSlate
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = AirbnbCharcoal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = footnote,
                style = MaterialTheme.typography.bodyMedium,
                color = AirbnbSlate
            )
        }
    }
}

@Composable
private fun MinimalPermissionsSection(
    context: Context,
    isOverlayGranted: Boolean,
    isAccessibilityGranted: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AirbnbWhite,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Instagram connection",
                style = MaterialTheme.typography.titleLarge,
                color = AirbnbCharcoal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enable these once so ReelGuard can time your Instagram sessions and float the minimal pill after your grace period.",
                style = MaterialTheme.typography.bodyMedium,
                color = AirbnbSlate
            )

            Spacer(modifier = Modifier.height(14.dp))

            MinimalPermissionRow(
                title = "Overlay permission",
                subtitle = if (isOverlayGranted) "Connected" else "Allows floating pill over Instagram",
                isGranted = isOverlayGranted,
                onClick = {
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )

            HorizontalDivider(
                color = AirbnbDivider,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            MinimalPermissionRow(
                title = "Reels scroll detector",
                subtitle = if (isAccessibilityGranted) "Connected" else "Counts vertical Reel swipes",
                isGranted = isAccessibilityGranted,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

@Composable
private fun MinimalPermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AirbnbCharcoal,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) StageFreshGreen else AirbnbSlate
            )
        }
        if (isGranted) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(StageFreshGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = StageFreshGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Text(
                text = "Set up",
                style = MaterialTheme.typography.labelLarge,
                color = AirbnbRausch
            )
        }
    }
}
