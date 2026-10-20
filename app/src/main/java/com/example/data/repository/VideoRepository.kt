package com.example.data.repository

import android.content.Context
import com.example.data.local.ParentPreferences
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.remote.CloudSyncState
import com.example.data.remote.FirestoreSyncManager
import com.example.domain.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val profileDao: com.example.data.local.ProfileDao,
    private val parentPreferences: ParentPreferences,
    private val watchHistoryDao: com.example.data.local.WatchHistoryDao = com.example.data.local.AppDatabase.getDatabase(context).watchHistoryDao(),
    val syncManager: FirestoreSyncManager = FirestoreSyncManager(
        context = context,
        videoDao = videoDao,
        profileDao = profileDao,
        syncQueueDao = com.example.data.local.AppDatabase.getDatabase(context).syncQueueDao(),
        watchHistoryDao = watchHistoryDao
    )
) {

    init {
        com.example.data.remote.CloudSyncWorker.schedulePeriodicSync(context)
        CoroutineScope(Dispatchers.IO).launch {
            cleanFakeVideosAndEnsureReal()
        }
    }

    private suspend fun cleanFakeVideosAndEnsureReal() {
        try {
            com.example.data.local.InitialDataSeed.FAKE_YOUTUBE_IDS.forEach { fakeId ->
                videoDao.deleteByYoutubeId(fakeId)
                syncManager.recordDeletedVideo(fakeId)
            }
            if (videoDao.getVideoByYoutubeId("9iW5fBH6PRQ") == null) {
                videoDao.insertVideos(com.example.data.local.InitialDataSeed.getInitialVideos())
                syncManager.pushAllVideosToCloud(getFamilyCode())
            }
        } catch (_: Exception) {}
    }

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
        syncManager.recordDeletedProfile(profile.id)
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
        syncManager.recordDeletedVideo(video.youtubeId)
        videoDao.deleteVideo(VideoEntity.fromDomain(video))
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun deleteById(id: Long) {
        // Find video entity to get youtubeId for deletion record
        videoDao.getAllVideosList().find { it.id == id }?.let { entity ->
            syncManager.recordDeletedVideo(entity.youtubeId)
        }
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

    fun getWatchHistoryForProfile(profileId: String): Flow<List<com.example.data.local.WatchHistoryEntity>> {
        return watchHistoryDao.getWatchHistoryForProfile(profileId)
    }

    fun getUnfinishedHistoryForProfile(profileId: String): Flow<List<com.example.data.local.WatchHistoryEntity>> {
        return watchHistoryDao.getUnfinishedHistoryForProfile(profileId)
    }

    fun getWatchProgress(profileId: String, youtubeId: String): Flow<com.example.data.local.WatchHistoryEntity?> {
        return watchHistoryDao.getWatchProgress(profileId, youtubeId)
    }

    suspend fun updateWatchProgress(
        profileId: String,
        youtubeId: String,
        positionSeconds: Long,
        durationSeconds: Long
    ) {
        if (profileId.isBlank() || youtubeId.isBlank()) return
        val existing = watchHistoryDao.getWatchProgressDirect(profileId, youtubeId)
        val pct = if (durationSeconds > 0) ((positionSeconds * 100) / durationSeconds).toInt().coerceIn(0, 100) else 0
        val isFinished = pct >= 95 || (durationSeconds > 0 && (durationSeconds - positionSeconds) <= 10)
        
        val newEntity = com.example.data.local.WatchHistoryEntity(
            profileId = profileId,
            youtubeId = youtubeId,
            positionSeconds = positionSeconds,
            durationSeconds = durationSeconds,
            progressPercentage = pct,
            lastWatchedAt = System.currentTimeMillis(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            isFinished = isFinished
        )
        watchHistoryDao.upsertWatchHistory(newEntity)
        syncManager.pushAllVideosToCloud(getFamilyCode())
    }

    suspend fun removeFromResume(profileId: String, youtubeId: String) {
        if (profileId.isBlank() || youtubeId.isBlank()) return
        watchHistoryDao.deleteWatchHistory(profileId, youtubeId)
        syncManager.pushAllVideosToCloud(getFamilyCode())
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
