package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BrainRotStage
import com.example.data.DailyScrollLogEntity
import com.example.data.ReelSessionState
import com.example.ui.theme.AirbnbBorder
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbSurface
import com.example.ui.theme.AirbnbWhite

@Composable
fun ThresholdsScreen(
    state: ReelSessionState,
    dailyLogs: List<DailyScrollLogEntity>,
    onUpdateSettings: (Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var delayMins by remember(state.bannerDelayMinutes) {
        mutableIntStateOf(state.bannerDelayMinutes)
    }
    var warningVal by remember(state.warningThreshold) {
        mutableIntStateOf(state.warningThreshold)
    }
    var criticalVal by remember(state.criticalThreshold) {
        mutableIntStateOf(state.criticalThreshold)
    }
    var autoExitVal by remember(state.autoExitThreshold) {
        mutableIntStateOf(state.autoExitThreshold)
    }
    var autoExitEnabled by remember(state.autoExitOnLimit) {
        mutableStateOf(state.autoExitOnLimit)
    }
    var hapticsEnabled by remember(state.hapticFeedbackEnabled) {
        mutableStateOf(state.hapticFeedbackEnabled)
    }

    val commitChanges = {
        val d = delayMins.coerceIn(1, 60)
        val w = warningVal.coerceAtLeast(2)
        val c = criticalVal.coerceAtLeast(w + 2)
        val a = autoExitVal.coerceAtLeast(c + 2)
        delayMins = d
        warningVal = w
        criticalVal = c
        autoExitVal = a
        onUpdateSettings(d, w, c, a, autoExitEnabled, hapticsEnabled)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AirbnbWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Airbnb-Style Stepper Section for Banner Appearance Delay & Reel Thresholds
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AirbnbWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Timing & limits",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AirbnbCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize when the floating banner appears and how many reels trigger each brain rot stage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AirbnbSlate
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Configurable Banner Appearance Delay (Default: 5 minutes)
                    AirbnbStepperRow(
                        title = "Banner appearance delay",
                        subtitle = "Wait time after opening Instagram before showing banner",
                        valueText = "$delayMins min",
                        canDecrement = delayMins > 1,
                        canIncrement = delayMins < 60,
                        onDecrement = {
                            delayMins = (delayMins - 1).coerceAtLeast(1)
                            commitChanges()
                        },
                        onIncrement = {
                            delayMins = (delayMins + 1).coerceAtMost(60)
                            commitChanges()
                        },
                        decrementTag = "delay_minus_button",
                        incrementTag = "delay_plus_button",
                        valueTag = "delay_minutes_value"
                    )

                    HorizontalDivider(
                        color = AirbnbDivider,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // 2. Stage 3 Brain Fog Threshold
                    AirbnbStepperRow(
                        title = "Brain fog warning",
                        subtitle = "Reels scrolled to enter Stage 3",
                        valueText = "$warningVal reels",
                        canDecrement = warningVal > 2,
                        canIncrement = warningVal < 40,
                        onDecrement = {
                            warningVal = (warningVal - 1).coerceAtLeast(2)
                            commitChanges()
                        },
                        onIncrement = {
                            warningVal += 1
                            if (criticalVal < warningVal + 2) criticalVal = warningVal + 2
                            if (autoExitVal < criticalVal + 2) autoExitVal = criticalVal + 2
                            commitChanges()
                        }
                    )

                    HorizontalDivider(
                        color = AirbnbDivider,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // 3. Stage 4 High Brain Rot Threshold
                    AirbnbStepperRow(
                        title = "High brain rot alert",
                        subtitle = "Reels scrolled to enter Stage 4",
                        valueText = "$criticalVal reels",
                        canDecrement = criticalVal > warningVal + 2,
                        canIncrement = criticalVal < 80,
                        onDecrement = {
                            criticalVal = (criticalVal - 1).coerceAtLeast(warningVal + 2)
                            commitChanges()
                        },
                        onIncrement = {
                            criticalVal += 1
                            if (autoExitVal < criticalVal + 2) autoExitVal = criticalVal + 2
                            commitChanges()
                        }
                    )

                    HorizontalDivider(
                        color = AirbnbDivider,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // 4. Stage 5 Auto-Exit Hard Limit
                    AirbnbStepperRow(
                        title = "Auto-exit limit",
                        subtitle = "Reels scrolled before returning to Home",
                        valueText = "$autoExitVal reels",
                        canDecrement = autoExitVal > criticalVal + 2,
                        canIncrement = autoExitVal < 150,
                        onDecrement = {
                            autoExitVal = (autoExitVal - 1).coerceAtLeast(criticalVal + 2)
                            commitChanges()
                        },
                        onIncrement = {
                            autoExitVal += 1
                            commitChanges()
                        }
                    )
                }
            }
        }

        // Clean Toggle Preferences
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AirbnbWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        color = AirbnbCharcoal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-exit on limit",
                                style = MaterialTheme.typography.titleMedium,
                                color = AirbnbCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically close Instagram when you reach $autoExitVal reels.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AirbnbSlate
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = autoExitEnabled,
                            onCheckedChange = {
                                autoExitEnabled = it
                                commitChanges()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AirbnbCharcoal,
                                uncheckedThumbColor = AirbnbSlate,
                                uncheckedTrackColor = AirbnbSurface
                            ),
                            modifier = Modifier.testTag("auto_exit_limit_switch")
                        )
                    }

                    HorizontalDivider(
                        color = AirbnbDivider,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Subtle haptic tap",
                                style = MaterialTheme.typography.titleMedium,
                                color = AirbnbCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Gentle vibration feedback when brain rot stages escalate.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AirbnbSlate
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                hapticsEnabled = it
                                commitChanges()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AirbnbCharcoal,
                                uncheckedThumbColor = AirbnbSlate,
                                uncheckedTrackColor = AirbnbSurface
                            ),
                            modifier = Modifier.testTag("haptic_buzz_switch")
                        )
                    }
                }
            }
        }

        // Daily Logs
        if (dailyLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Daily history",
                    style = MaterialTheme.typography.titleLarge,
                    color = AirbnbCharcoal
                )
            }
            items(dailyLogs, key = { it.dateKey }) { log ->
                val peakStage = BrainRotStage.entries.firstOrNull { it.level == log.peakBrainRotLevel }
                    ?: BrainRotStage.MINDFUL
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = log.dateKey,
                                style = MaterialTheme.typography.titleMedium,
                                color = AirbnbCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${log.reelsScrolled} reels · ${log.homeExitsTriggered} exits",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AirbnbSlate
                            )
                        }
                        Text(
                            text = peakStage.shortTag,
                            style = MaterialTheme.typography.labelLarge,
                            color = peakStage.color
                        )
                    }
                    HorizontalDivider(color = AirbnbDivider)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Iconic Airbnb-style circular + / - stepper control row.
 */
@Composable
private fun AirbnbStepperRow(
    title: String,
    subtitle: String,
    valueText: String,
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementTag: String = "",
    incrementTag: String = "",
    valueTag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AirbnbSlate
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Minus Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (canDecrement) AirbnbBorder else AirbnbDivider,
                        shape = CircleShape
                    )
                    .clickable(enabled = canDecrement) { onDecrement() }
                    .let { if (decrementTag.isNotEmpty()) it.testTag(decrementTag) else it },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = if (canDecrement) AirbnbCharcoal else AirbnbDivider,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                color = AirbnbCharcoal,
                fontWeight = FontWeight.SemiBold,
                modifier = if (valueTag.isNotEmpty()) Modifier.testTag(valueTag) else Modifier
            )

            // Circular Plus Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (canIncrement) AirbnbBorder else AirbnbDivider,
                        shape = CircleShape
                    )
                    .clickable(enabled = canIncrement) { onIncrement() }
                    .let { if (incrementTag.isNotEmpty()) it.testTag(incrementTag) else it },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = if (canIncrement) AirbnbCharcoal else AirbnbDivider,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
