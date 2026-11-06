package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StageFogTerracotta
import com.example.ui.theme.StageFreshGreen
import com.example.ui.theme.StageMeltdownWine
import com.example.ui.theme.StageRotRausch
import com.example.ui.theme.StageSpikeAmber

enum class BrainRotStage(
    val level: Int,
    val title: String,
    val badgeEmoji: String,
    val shortTag: String,
    val description: String,
    val color: Color,
    val hexColor: Int
) {
    MINDFUL(
        level = 1,
        title = "Mindful Browsing",
        badgeEmoji = "🌱",
        shortTag = "Fresh",
        description = "Focused and intentional. You're in complete control of your time.",
        color = StageFreshGreen,
        hexColor = 0xFF008A05.toInt()
    ),
    DOPAMINE_SPIKE(
        level = 2,
        title = "Dopamine Loop",
        badgeEmoji = "⚡",
        shortTag = "Drifting",
        description = "Short-form novelty loop active. Notice your thumb scrolling automatically.",
        color = StageSpikeAmber,
        hexColor = 0xFFE07912.toInt()
    ),
    BRAIN_FOG(
        level = 3,
        title = "Brain Fog",
        badgeEmoji = "☁️",
        shortTag = "Foggy",
        description = "Autopilot scrolling detected. You likely won't recall the last 5 reels.",
        color = StageFogTerracotta,
        hexColor = 0xFFC13515.toInt()
    ),
    SEVERE_ROT(
        level = 4,
        title = "High Brain Rot",
        badgeEmoji = "🥀",
        shortTag = "High Rot",
        description = "Attention span depleted. Time to step away and reset your focus.",
        color = StageRotRausch,
        hexColor = 0xFFFF385C.toInt()
    ),
    COGNITIVE_MELTDOWN(
        level = 5,
        title = "Overload Limit",
        badgeEmoji = "🛑",
        shortTag = "Overload",
        description = "Maximum scroll limit reached. Stepping back to the home screen.",
        color = StageMeltdownWine,
        hexColor = 0xFF92174D.toInt()
    );

    companion object {
        fun fromReelsCount(
            count: Int,
            warningThreshold: Int = 5,
            criticalThreshold: Int = 15,
            autoExitThreshold: Int = 25
        ): BrainRotStage {
            val spikeStart = (warningThreshold / 2).coerceAtLeast(2)
            return when {
                count >= autoExitThreshold -> COGNITIVE_MELTDOWN
                count >= criticalThreshold -> SEVERE_ROT
                count >= warningThreshold -> BRAIN_FOG
                count >= spikeStart -> DOPAMINE_SPIKE
                else -> MINDFUL
            }
        }
    }
}
