package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.domain.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

import com.example.data.local.ProfileDao
import com.example.data.local.ProfileEntity

enum class CloudSyncState {
    CONNECTED, SYNCING, OFFLINE, ERROR
}

class FirestoreSyncManager(
    private val context: Context,
    private val videoDao: VideoDao,
    private val profileDao: ProfileDao? = null
) {

    private val _syncState = MutableStateFlow(CloudSyncState.OFFLINE)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null
    private var currentFamilyCode: String? = null
    private var cachedBlobId: String? = null

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()

    private companion object {
        private const val TAG = "FirestoreSyncManager"
        private const val KEYVALUE_URL = "https://keyvalue.immanuel.co/api/KeyVal"
        private const val JSONBLOB_URL = "https://jsonblob.com/api/jsonBlob"
        private const val APP_KEY = "youtubeprive_v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    fun startSync(familyCode: String) {
        if (familyCode.isBlank()) return
        if (currentFamilyCode == familyCode && syncJob?.isActive == true) return

        stopSync()
        currentFamilyCode = familyCode
        cachedBlobId = null
        _syncState.value = CloudSyncState.SYNCING

        syncJob = scope.launch {
            Log.d(TAG, "Starting cloud sync loop for family code: $familyCode")
            while (isActive && currentFamilyCode == familyCode) {
                try {
                    syncWithCloud(familyCode)
                } catch (e: Throwable) {
                    Log.w(TAG, "Error during sync cycle: ${e.message}")
                    _syncState.value = CloudSyncState.OFFLINE
                }
                delay(3000) // Poll every 3 seconds for fast real-time updates
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
        currentFamilyCode = null
        cachedBlobId = null
        _syncState.value = CloudSyncState.OFFLINE
    }

    private suspend fun getOrCreateBlobId(familyCode: String): String? = withContext(Dispatchers.IO) {
        cachedBlobId?.let { return@withContext it }

        try {
            // Step 1: Check keyvalue store for existing blob_id mapping
            val getUrl = "$KEYVALUE_URL/GetValue/$APP_KEY/$familyCode"
            val request = Request.Builder().url(getUrl).get().build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val rawBody = response.body?.string()?.trim()?.trim('"') ?: ""
                if (rawBody.length >= 20 && rawBody.contains("-")) {
                    // Verify if this blob actually exists on jsonblob.com
                    val checkUrl = "$JSONBLOB_URL/$rawBody"
                    val checkReq = Request.Builder().url(checkUrl).get().build()
                    val checkResp = httpClient.newCall(checkReq).execute()
                    if (checkResp.isSuccessful) {
                        cachedBlobId = rawBody
                        return@withContext rawBody
                    }
                    Log.w(TAG, "Existing blobId $rawBody for family code $familyCode expired or returned ${checkResp.code}. Recreating.")
                }
            }

            // Step 2: Create a new JSON blob if mapping doesn't exist or expired
            return@withContext createAndStoreNewBlobId(familyCode)
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting or creating blob ID for family code: $familyCode", e)
        }
        return@withContext null
    }

    private fun createAndStoreNewBlobId(familyCode: String): String? {
        try {
            val emptyBody = "[]".toRequestBody(JSON_MEDIA_TYPE)
            val createRequest = Request.Builder().url(JSONBLOB_URL).post(emptyBody).build()
            val createResponse = httpClient.newCall(createRequest).execute()

            var newBlobId: String? = createResponse.header("X-jsonblob-id")
            if (newBlobId.isNull_or_blank()) {
                val location = createResponse.header("Location") ?: ""
                newBlobId = location.substringAfterLast("/")
            }

            if (!newBlobId.isNull_or_blank()) {
                // Store mapping in keyvalue store using valid JSON content-type & body
                val updateUrl = "$KEYVALUE_URL/UpdateValue/$APP_KEY/$familyCode/$newBlobId"
                val dummyBody = "{}".toRequestBody(JSON_MEDIA_TYPE)
                val updateRequest = Request.Builder().url(updateUrl).post(dummyBody).build()
                val updateResp = httpClient.newCall(updateRequest).execute()
                Log.d(TAG, "KeyVal UpdateValue result code: ${updateResp.code}")

                cachedBlobId = newBlobId
                Log.d(TAG, "Created new cloud sync blob ID: $newBlobId for family code: $familyCode")
                return newBlobId
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error creating/storing new blob ID", e)
        }
        return null
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty() || this == "null"
    }

    private suspend fun syncWithCloud(familyCode: String) = withContext(Dispatchers.IO) {
        val blobId = getOrCreateBlobId(familyCode)
        if (blobId == null) {
            _syncState.value = CloudSyncState.OFFLINE
            return@withContext
        }

        try {
            val getBlobUrl = "$JSONBLOB_URL/$blobId"
            val request = Request.Builder().url(getBlobUrl).get().build()
            val response = httpClient.newCall(request).execute()

            if (response.code == 404) {
                // Blob expired or not found, recreate new blob immediately and push local videos
                cachedBlobId = null
                val newBlobId = createAndStoreNewBlobId(familyCode)
                if (newBlobId != null) {
                    val localVideos = videoDao.getAllVideosList()
                    pushLocalVideosToCloudInternal(newBlobId, localVideos)
                } else {
                    _syncState.value = CloudSyncState.OFFLINE
                }
                return@withContext
            }

            if (!response.isSuccessful) {
                _syncState.value = CloudSyncState.OFFLINE
                return@withContext
            }

            val jsonString = response.body?.string()?.trim() ?: "[]"

            val jsonArray: JSONArray
            if (jsonString.startsWith("{")) {
                val rootObj = JSONObject(jsonString)
                jsonArray = rootObj.optJSONArray("videos") ?: JSONArray()
                
                // Sync profiles if present
                val profilesArray = rootObj.optJSONArray("profiles")
                if (profilesArray != null && profileDao != null) {
                    val remoteProfiles = mutableListOf<ProfileEntity>()
                    val remoteProfileIds = mutableListOf<Long>()
                    for (p in 0 until profilesArray.length()) {
                        val pObj = profilesArray.getJSONObject(p)
                        val pName = pObj.optString("name")
                        if (pName.isNotBlank()) {
                            val pId = pObj.optLong("id", 0L)
                            val entity = ProfileEntity(
                                id = pId,
                                name = pName,
                                avatarEmoji = pObj.optString("avatarEmoji", "👦"),
                                age = pObj.optInt("age", 5),
                                createdAt = pObj.optLong("createdAt", System.currentTimeMillis())
                            )
                            remoteProfiles.add(entity)
                            if (pId > 0L) remoteProfileIds.add(pId)
                        }
                    }
                    if (remoteProfiles.isNotEmpty()) {
                        profileDao.insertProfiles(remoteProfiles)
                    }
                }
            } else {
                jsonArray = JSONArray(jsonString)
            }

            if (jsonArray.length() == 0) {
                // Remote blob is empty. If local DB has videos or profiles, push to cloud
                val localVideos = videoDao.getAllVideosList()
                if (localVideos.isNotEmpty() || (profileDao?.getAllProfilesList()?.isNotEmpty() == true)) {
                    pushLocalVideosToCloudInternal(blobId, localVideos)
                }
                _syncState.value = CloudSyncState.CONNECTED
                return@withContext
            }

            // Remote has videos. Update local database
            val remoteEntities = mutableListOf<VideoEntity>()
            val remoteYoutubeIds = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val youtubeId = obj.optString("youtubeId")
                if (youtubeId.isBlank()) continue

                val existing = videoDao.getVideoByYoutubeId(youtubeId)
                val localId = existing?.id ?: 0L

                val entity = VideoEntity(
                    id = localId,
                    youtubeId = youtubeId,
                    title = obj.optString("title", "Vidéo"),
                    thumbnailUrl = obj.optString("thumbnailUrl", "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"),
                    category = obj.optString("category", "Général"),
                    favorite = obj.optBoolean("favorite", false),
                    duration = obj.optString("duration", ""),
                    lastPlayed = existing?.lastPlayed ?: obj.optLong("lastPlayed", 0L),
                    ageMin = obj.optInt("ageMin", 3),
                    ageMax = obj.optInt("ageMax", 10),
                    displayOrder = obj.optInt("displayOrder", i),
                    description = obj.optString("description", ""),
                    targetProfileId = obj.optLong("targetProfileId", 0L),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                remoteEntities.add(entity)
                remoteYoutubeIds.add(youtubeId)
            }

            if (remoteEntities.isNotEmpty()) {
                videoDao.insertVideos(remoteEntities)
                videoDao.deleteVideosNotIn(remoteYoutubeIds)
            }

            _syncState.value = CloudSyncState.CONNECTED
        } catch (e: Throwable) {
            Log.w(TAG, "Cloud sync exception: ${e.message}")
            _syncState.value = CloudSyncState.OFFLINE
        }
    }

    fun pushAllVideosToCloud(familyCode: String) {
        if (familyCode.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                val blobId = getOrCreateBlobId(familyCode)
                if (blobId != null) {
                    val localVideos = videoDao.getAllVideosList()
                    pushLocalVideosToCloudInternal(blobId, localVideos)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error pushing all videos to cloud", e)
            }
        }
    }

    fun addOrUpdateVideoInCloud(familyCode: String, video: Video) {
        pushAllVideosToCloud(familyCode)
    }

    fun deleteVideoFromCloud(familyCode: String, youtubeId: String) {
        pushAllVideosToCloud(familyCode)
    }

    private suspend fun pushLocalVideosToCloudInternal(blobId: String, localVideos: List<VideoEntity>) {
        try {
            val rootObj = JSONObject()

            val jsonArray = JSONArray()
            localVideos.forEachIndexed { index, video ->
                val obj = JSONObject().apply {
                    put("youtubeId", video.youtubeId)
                    put("title", video.title)
                    put("thumbnailUrl", video.thumbnailUrl)
                    put("category", video.category)
                    put("favorite", video.favorite)
                    put("duration", video.duration)
                    put("lastPlayed", video.lastPlayed)
                    put("ageMin", video.ageMin)
                    put("ageMax", video.ageMax)
                    put("displayOrder", video.displayOrder)
                    put("description", video.description)
                    put("targetProfileId", video.targetProfileId)
                    put("createdAt", video.createdAt)
                }
                jsonArray.put(obj)
            }
            rootObj.put("videos", jsonArray)

            val profilesArray = JSONArray()
            if (profileDao != null) {
                val localProfiles = profileDao.getAllProfilesList()
                localProfiles.forEach { p ->
                    val pObj = JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("avatarEmoji", p.avatarEmoji)
                        put("age", p.age)
                        put("createdAt", p.createdAt)
                    }
                    profilesArray.put(pObj)
                }
            }
            rootObj.put("profiles", profilesArray)

            val jsonString = rootObj.toString()
            val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)
            val putUrl = "$JSONBLOB_URL/$blobId"
            val request = Request.Builder().url(putUrl).put(requestBody).build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully pushed ${localVideos.size} videos to cloud (blobId: $blobId)")
                _syncState.value = CloudSyncState.CONNECTED
            } else {
                Log.w(TAG, "Failed to push videos to cloud blob $blobId. Response code: ${response.code}")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception pushing local videos to cloud blob $blobId", e)
        }
    }
}
