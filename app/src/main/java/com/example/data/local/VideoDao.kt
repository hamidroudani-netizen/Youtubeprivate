package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY displayOrder ASC, createdAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE favorite = 1 ORDER BY displayOrder ASC, createdAt DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY displayOrder ASC, createdAt DESC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: Long): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE youtubeId = :youtubeId LIMIT 1")
    suspend fun getVideoByYoutubeId(youtubeId: String): VideoEntity?

    @Query("SELECT * FROM videos ORDER BY displayOrder ASC, createdAt DESC")
    suspend fun getAllVideosList(): List<VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM videos WHERE youtubeId NOT IN (:youtubeIds)")
    suspend fun deleteVideosNotIn(youtubeIds: List<String>)

    @Query("DELETE FROM videos WHERE youtubeId = :youtubeId")
    suspend fun deleteByYoutubeId(youtubeId: String)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("UPDATE videos SET favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET lastPlayed = :timestamp WHERE id = :id")
    suspend fun updateLastPlayed(id: Long, timestamp: Long)
}
