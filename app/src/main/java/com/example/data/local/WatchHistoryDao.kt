package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE profileId = :profileId ORDER BY lastWatchedAt DESC")
    fun getWatchHistoryForProfile(profileId: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE profileId = :profileId AND isFinished = 0 ORDER BY lastWatchedAt DESC")
    fun getUnfinishedHistoryForProfile(profileId: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE profileId = :profileId AND youtubeId = :youtubeId LIMIT 1")
    fun getWatchProgress(profileId: String, youtubeId: String): Flow<WatchHistoryEntity?>

    @Query("SELECT * FROM watch_history WHERE profileId = :profileId AND youtubeId = :youtubeId LIMIT 1")
    suspend fun getWatchProgressDirect(profileId: String, youtubeId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history")
    suspend fun getAllWatchHistoryList(): List<WatchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(item: WatchHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistoryList(items: List<WatchHistoryEntity>)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId AND youtubeId = :youtubeId")
    suspend fun deleteWatchHistory(profileId: String, youtubeId: String)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId")
    suspend fun clearWatchHistoryForProfile(profileId: String)
}
