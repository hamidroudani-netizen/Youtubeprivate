package com.example.data.repository

import android.content.Context
import com.example.data.local.ParentPreferences
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.remote.CloudSyncState
import com.example.data.remote.FirestoreSyncManager
import com.example.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.regex.Pattern

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val profileDao: com.example.data.local.ProfileDao,
    private val parentPreferences: ParentPreferences,
    val syncManager: FirestoreSyncManager = FirestoreSyncManager(context, videoDao, profileDao)
) {

    val allProfiles: Flow<List<com.example.domain.model.ChildProfile>> = profileDao.getAllProfiles().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertProfile(profile: com.example.domain.model.ChildProfile): Long {
        val id = profileDao.insertProfile(com.example.data.local.ProfileEntity.fromDomain(profile))
        syncManager.pushAllVideosToCloud(getFamilyCode())
        return id
    }

    suspend fun updateProfile(profile: com.example.domain.model.ChildProfile) {
        profileDao.updateProfile(com.example.data.local.ProfileEntity.fromDomain(profile))
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun deleteProfile(profile: com.example.domain.model.ChildProfile) {
        profileDao.deleteProfile(com.example.data.local.ProfileEntity.fromDomain(profile))
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    val allVideos: Flow<List<Video>> = videoDao.getAllVideos().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteVideos: Flow<List<Video>> = videoDao.getFavoriteVideos().map { list ->
        list.map { it.toDomain() }
    }

    val cloudSyncState: StateFlow<CloudSyncState> = syncManager.syncState

    fun getFamilyCode(): String {
        return parentPreferences.getFamilyCode()
    }

    fun updateFamilyCode(newCode: String): Boolean {
        val success = parentPreferences.setFamilyCode(newCode)
        if (success) {
            syncManager.startSync(newCode)
        }
        return success
    }

    fun generateNewFamilyCode(): String {
        val newCode = parentPreferences.generateNewFamilyCode()
        syncManager.startSync(newCode)
        return newCode
    }

    fun startCloudSync() {
        syncManager.startSync(getFamilyCode())
    }

    fun getVideosByCategory(category: String): Flow<List<Video>> {
        return videoDao.getVideosByCategory(category).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getVideoById(id: Long): Flow<Video?> {
        return videoDao.getVideoById(id).map { it?.toDomain() }
    }

    private val channelFetcher = com.example.data.remote.YouTubeChannelFetcher()

    suspend fun fetchChannelVideos(input: String): Result<com.example.data.remote.ImportedChannelResult> {
        return channelFetcher.fetchChannelVideos(input)
    }

    suspend fun insertVideo(video: Video): Long {
        val rowId = videoDao.insertVideo(VideoEntity.fromDomain(video))
        syncManager.pushAllVideosToCloud(getFamilyCode())
        return rowId
    }

    suspend fun importMultipleVideos(videos: List<Video>) {
        if (videos.isEmpty()) return
        val entities = videos.map { VideoEntity.fromDomain(it) }
        videoDao.insertVideos(entities)
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun updateVideo(video: Video) {
        videoDao.updateVideo(VideoEntity.fromDomain(video))
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun deleteVideo(video: Video) {
        videoDao.deleteVideo(VideoEntity.fromDomain(video))
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun deleteById(id: Long) {
        videoDao.deleteVideoById(id)
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        videoDao.updateFavoriteStatus(id, !currentFavorite)
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun updateLastPlayed(id: Long) {
        videoDao.updateLastPlayed(id, System.currentTimeMillis())
    }

    suspend fun updateVideoOrders(videos: List<Video>) {
        val entities = videos.mapIndexed { index, video ->
            VideoEntity.fromDomain(video.copy(order = index + 1))
        }
        videoDao.insertVideos(entities)
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    companion object {
        /**
         * Extracts YouTube video ID from various standard YouTube URL formats
         */
        fun extractYoutubeId(inputUrl: String): String? {
            val trimmed = inputUrl.trim()
            if (trimmed.isBlank()) return null

            // Direct ID check (YouTube IDs are typically 11 chars)
            if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                return trimmed
            }

            val pattern = Pattern.compile(
                "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/.+/|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(trimmed)
            return if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        }

        fun generateThumbnailUrl(youtubeId: String): String {
            return "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"
        }
    }
}
