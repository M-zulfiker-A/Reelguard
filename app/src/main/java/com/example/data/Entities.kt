package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_messages")
data class CustomMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val messageText: String,
    val category: String = "Reality Check",
    val isEnabled: Boolean = true,
    val minStageLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_scroll_logs")
data class DailyScrollLogEntity(
    @PrimaryKey val dateKey: String, // Format: YYYY-MM-DD
    val reelsScrolled: Int = 0,
    val homeExitsTriggered: Int = 0,
    val peakBrainRotLevel: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ScrollEventItem(
    val id: Long = System.currentTimeMillis(),
    val timestampLabel: String,
    val sourceLabel: String,
    val reelNumber: Int,
    val stage: BrainRotStage,
    val bannerMessageShown: String
)
