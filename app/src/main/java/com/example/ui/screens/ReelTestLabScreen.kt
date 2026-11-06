package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ReelSessionState
import com.example.ui.components.PersistentBrainRotBanner
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbSurface
import com.example.ui.theme.AirbnbWhite
import kotlinx.coroutines.delay

private data class SimulatedReelPost(
    val authorHandle: String,
    val title: String,
    val caption: String,
    val audioTrack: String,
    val likesCount: String,
    val commentsCount: String,
    val gradientColors: List<Color>,
    val emojiArt: String
)

private val simulatedReelsPool = listOf(
    SimulatedReelPost(
        authorHandle = "@minimal_spaces",
        title = "Inside a Sunlit Kyoto Courtyard Home",
        caption = "Cedar joinery and morning light. Would you stay here for a month?",
        audioTrack = "Acoustic Morning • Instrumental",
        likesCount = "142K",
        commentsCount = "1,290",
        gradientColors = listOf(Color(0xFF2C3E50), Color(0xFF34495E)),
        emojiArt = "🏡🌿☀️"
    ),
    SimulatedReelPost(
        authorHandle = "@satisfying_loops_3am",
        title = "POV: Pressure Washing a Flagstone Patio",
        caption = "Wait for the corner spot at the end... #satisfying #reels",
        audioTrack = "Original Audio • Ambient Loop",
        likesCount = "412K",
        commentsCount = "3,891",
        gradientColors = listOf(Color(0xFF1F2937), Color(0xFF111827)),
        emojiArt = "💦🧱✨"
    ),
    SimulatedReelPost(
        authorHandle = "@midnight_espresso",
        title = "Pulling the Perfect Morning Cortado",
        caption = "Dialing in a fresh Ethiopian light roast at 9 bars.",
        audioTrack = "Lo-Fi Cafe • Vinyl Crackle",
        likesCount = "89K",
        commentsCount = "640",
        gradientColors = listOf(Color(0xFF3E2723), Color(0xFF1B0000)),
        emojiArt = "☕🥛✨"
    ),
    SimulatedReelPost(
        authorHandle = "@alpine_cabins",
        title = "Snowfall at 2,400 Meters in the Dolomites",
        caption = "Wood stove crackling while the storm rolls in outside.",
        audioTrack = "Original Audio • Fireplace & Wind",
        likesCount = "530K",
        commentsCount = "4,120",
        gradientColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
        emojiArt = "🏔️❄️🔥"
    ),
    SimulatedReelPost(
        authorHandle = "@infinite_scroll_trap",
        title = "Why We Keep Scrolling Even When Tired",
        caption = "Variable reward schedules explained in 15 seconds...",
        audioTrack = "Soft Piano • Reflection",
        likesCount = "95.4K",
        commentsCount = "2,310",
        gradientColors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B)),
        emojiArt = "🧠⏳🍃"
    )
)

@Composable
fun ReelTestLabScreen(
    state: ReelSessionState,
    onSimulateReelSwipe: () -> Unit,
    onTickOneSecond: () -> Unit,
    onSkipDelayClicked: () -> Unit,
    onExitInstagramClicked: () -> Unit,
    onShuffleQuote: () -> Unit,
    onResetCounter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var feedIndex by remember { mutableIntStateOf(0) }
    var cumulativeDragY by remember { mutableFloatStateOf(0f) }

    // Tick the simulated Instagram session timer every second while in the Test Lab
    LaunchedEffect(state.isShieldEnabled) {
        while (state.isShieldEnabled) {
            delay(1_000L)
            onTickOneSecond()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AirbnbWhite)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Delay Grace Window Status Bar (Explains why banner waits 5 mins, with 1-tap Fast-Forward)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AirbnbSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (state.isBannerDelayElapsed) AirbnbRausch else AirbnbSlate,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isBannerDelayElapsed) {
                            "${state.bannerDelayMinutes}m grace period elapsed · Top banner active"
                        } else {
                            "Banner hidden for ${state. formattedRemainingDelay} (waits ${state.bannerDelayMinutes}m)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbCharcoal
                    )
                }

                if (!state.isBannerDelayElapsed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AirbnbWhite)
                            .border(1.dp, AirbnbDivider, CircleShape)
                            .clickable { onSkipDelayClicked() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("skip_delay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Skip 5 minute delay",
                            tint = AirbnbRausch,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Skip ${state.bannerDelayMinutes}m wait",
                            style = MaterialTheme.typography.labelMedium,
                            color = AirbnbRausch,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Simulated Instagram Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, AirbnbDivider, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { cumulativeDragY = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            cumulativeDragY += dragAmount
                        },
                        onDragEnd = {
                            if (cumulativeDragY < -70f) {
                                feedIndex++
                                onSimulateReelSwipe()
                            }
                            cumulativeDragY = 0f
                        }
                    )
                }
                .testTag("simulated_instagram_reels_viewport")
        ) {
            AnimatedContent(
                targetState = feedIndex,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "reelCardTransition"
            ) { idx ->
                val post = simulatedReelsPool[idx % simulatedReelsPool.size]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(post.gradientColors))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = post.emojiArt,
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Reel #${idx + 1} · Session ${state.formattedElapsedSessionTime}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Swipe up or tap Next Reel below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Minimal Social Actions Column
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Likes",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = post.likesCount,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = post.commentsCount,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom-left Reel Details
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = post.authorHandle,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 2
                        )
                    }
                }
            }

            // Small, Minimal Top Banner: ONLY appears once the 5-minute (configurable) delay has elapsed!
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.isBannerDelayElapsed,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    PersistentBrainRotBanner(
                        state = state,
                        onExitClicked = onExitInstagramClicked,
                        onShuffleQuote = onShuffleQuote
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Airbnb-style Bottom Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    feedIndex++
                    onSimulateReelSwipe()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AirbnbCharcoal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("swipe_next_reel_button")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Next Reel (+15s)",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = {
                    feedIndex = 0
                    onResetCounter()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(46.dp)
                    .testTag("simulator_reset_button")
            ) {
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelLarge,
                    color = AirbnbCharcoal
                )
            }
        }
    }
}
