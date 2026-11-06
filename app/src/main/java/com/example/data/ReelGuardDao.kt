package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelGuardDao {

    @Query("SELECT * FROM custom_messages ORDER BY minStageLevel ASC, createdAt DESC")
    fun getAllMessages(): Flow<List<CustomMessageEntity>>

    @Query("SELECT * FROM custom_messages WHERE isEnabled = 1 ORDER BY minStageLevel ASC")
    suspend fun getEnabledMessagesSnapshot(): List<CustomMessageEntity>

    @Query("SELECT COUNT(*) FROM custom_messages")
    suspend fun getMessageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CustomMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<CustomMessageEntity>)

    @Update
    suspend fun updateMessage(message: CustomMessageEntity)

    @Delete
    suspend fun deleteMessage(message: CustomMessageEntity)

    @Query("SELECT * FROM daily_scroll_logs ORDER BY dateKey DESC LIMIT 14")
    fun getRecentDailyLogs(): Flow<List<DailyScrollLogEntity>>

    @Query("SELECT * FROM daily_scroll_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyLog(dateKey: String): DailyScrollLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyScrollLogEntity)
}
