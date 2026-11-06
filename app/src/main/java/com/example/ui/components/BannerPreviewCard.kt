package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReelSessionState
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbWhite

/**
 * Compact, whisper-minimal floating pill banner inspired by Airbnb's floating map/filter pill.
 * Single sleek horizontal bar with status dot, brain rot counter, short message, and minimal Exit pill.
 */
@Composable
fun PersistentBrainRotBanner(
    state: ReelSessionState,
    onExitClicked: () -> Unit,
    onShuffleQuote: () -> Unit,
    modifier: Modifier = Modifier,
    labelPrefix: String = ""
) {
    val stage = state.currentStage
    val animatedStageColor by animateColorAsState(
        targetValue = stage.color,
        animationSpec = tween(300),
        label = "stageDotColor"
    )

    Surface(
        shape = CircleShape,
        color = AirbnbWhite.copy(alpha = 0.98f),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(1.dp, AirbnbDivider, CircleShape)
            .clip(CircleShape)
            .clickable { onExitClicked() }
            .testTag("persistent_brain_rot_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Stage Indicator Dot + Compact Brain Rot Counter
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(animatedStageColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${state.sessionReelsScrolled}",
                    style = MaterialTheme.typography.labelLarge,
                    color = AirbnbCharcoal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("banner_reel_count_text")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "· ${stage.shortTag}",
                    style = MaterialTheme.typography.labelMedium,
                    color = animatedStageColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Thin Vertical Hairline Divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(1.dp)
                    .height(14.dp)
                    .background(AirbnbDivider)
            )

            // Center: Minimal Single-Line Configurable Wake-Up Quote
            Text(
                text = state.currentBannerMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = AirbnbSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .testTag("banner_message_text")
            )

            // Subtle Quote Cycle Icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onShuffleQuote() }
                    .testTag("shuffle_banner_message_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Next quote",
                    tint = AirbnbSlate,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Right: Minimal Airbnb Rausch "Exit" Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AirbnbRausch)
                    .clickable { onExitClicked() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("banner_exit_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Exit",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
