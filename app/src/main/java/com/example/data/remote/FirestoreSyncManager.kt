package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.ProfileDao
import com.example.data.local.ProfileEntity
import com.example.data.local.SyncQueueDao
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.local.WatchHistoryDao
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

enum class CloudSyncState {
    CONNECTED, SYNCING, OFFLINE, ERROR
}

class FirestoreSyncManager(
    private val context: Context,
    private val videoDao: VideoDao,
    private val profileDao: ProfileDao? = null,
    private val syncQueueDao: SyncQueueDao? = null,
    private val watchHistoryDao: WatchHistoryDao? = null
) {

    private val _syncState = MutableStateFlow(CloudSyncState.OFFLINE)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null
    private var currentFamilyCode: String? = null
    private var cachedObjectId: String? = null

    private val prefs = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    private val deletedYoutubeIds: MutableSet<String> = java.util.Collections.synchronizedSet(
        prefs.getStringSet("deleted_youtube_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    )

    private val deletedProfileIds: MutableSet<Long> = java.util.Collections.synchronizedSet(
        prefs.getStringSet("deleted_profile_ids", emptySet())?.mapNotNull { it.toLongOrNull() }?.toMutableSet() ?: mutableSetOf()
    )

    fun recordDeletedVideo(youtubeId: String) {
        if (youtubeId.isBlank()) return
        deletedYoutubeIds.add(youtubeId)
        prefs.edit().putStringSet("deleted_youtube_ids", deletedYoutubeIds.toSet()).apply()
    }

    fun recordDeletedProfile(profileId: Long) {
        if (profileId <= 0L) return
        deletedProfileIds.add(profileId)
        prefs.edit().putStringSet("deleted_profile_ids", deletedProfileIds.map { it.toString() }.toSet()).apply()
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private companion object {
        private const val TAG = "FirestoreSyncManager"
        private const val KEYVALUE_URL = "https://keyvalue.immanuel.co/api/KeyVal"
        private const val REST_API_URL = "https://api.restful-api.dev/objects"
        private const val APP_KEY = "youtubeprive_v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    fun startSync(familyCode: String) {
        val cleanCode = familyCode.replace(" ", "").trim()
        if (cleanCode.isBlank()) return
        if (currentFamilyCode == cleanCode && syncJob?.isActive == true) return

        stopSync()
        currentFamilyCode = cleanCode
        cachedObjectId = null
        _syncState.value = CloudSyncState.SYNCING

        syncJob = scope.launch {
            Log.d(TAG, "Démarrage de la synchronisation cloud pour le code famille: $cleanCode")
            while (isActive && currentFamilyCode == cleanCode) {
                try {
                    syncWithCloud(cleanCode)
                } catch (e: Throwable) {
                    Log.w(TAG, "Erreur cycle sync: ${e.message}")
                    _syncState.value = CloudSyncState.OFFLINE
                }
                delay(3000) // Vérification toutes les 3 secondes
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
        currentFamilyCode = null
        cachedObjectId = null
        _syncState.value = CloudSyncState.OFFLINE
    }

    suspend fun processSyncQueueAndSync(familyCode: String): Boolean = withContext(Dispatchers.IO) {
        val cleanCode = familyCode.replace(" ", "").trim()
        if (cleanCode.isBlank()) return@withContext false
        syncWithCloud(cleanCode)
    }

    private suspend fun getOrCreateObjectId(cleanCode: String): String? = withContext(Dispatchers.IO) {
        cachedObjectId?.let { return@withContext it }

        try {
            // Étape 1 : Vérifier si ce code famille a déjà un ID distant enregistré
            val getUrl = "$KEYVALUE_URL/GetValue/$APP_KEY/$cleanCode"
            val request = Request.Builder()
                .url(getUrl)
                .header("User-Agent", "YouTubePrivate-Android/1.0")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val rawBody = response.body?.string()?.trim()?.trim('"') ?: ""
                if (rawBody.length >= 10 && !rawBody.contains("null")) {
                    // Vérifier si cet objet existe toujours sur le serveur
                    val checkUrl = "$REST_API_URL/$rawBody"
                    val checkReq = Request.Builder()
                        .url(checkUrl)
                        .header("User-Agent", "YouTubePrivate-Android/1.0")
                        .get()
                        .build()
                    val checkResp = httpClient.newCall(checkReq).execute()
                    if (checkResp.isSuccessful) {
                        cachedObjectId = rawBody
                        Log.d(TAG, "ID distant existant validé: $rawBody pour la famille: $cleanCode")
                        return@withContext rawBody
                    }
                    Log.w(TAG, "L'objet $rawBody n'existe plus (${checkResp.code}). Recréation en cours.")
                }
            }

            // Étape 2 : Créer un nouvel objet distant
            return@withContext createAndStoreNewObjectId(cleanCode)
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur récupération/création ID pour le code: $cleanCode", e)
        }
        return@withContext null
    }

    private fun createAndStoreNewObjectId(cleanCode: String): String? {
        try {
            val initialPayload = JSONObject().apply {
                put("name", "family_$cleanCode")
                put("data", JSONObject().apply {
                    put("videos", JSONArray())
                    put("profiles", JSONArray())
                })
            }
            val body = initialPayload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val createRequest = Request.Builder()
                .url(REST_API_URL)
                .header("User-Agent", "YouTubePrivate-Android/1.0")
                .post(body)
                .build()
            val createResponse = httpClient.newCall(createRequest).execute()

            if (createResponse.isSuccessful) {
                val respString = createResponse.body?.string() ?: ""
                val respJson = JSONObject(respString)
                val newId = respJson.optString("id")

                if (newId.isNotBlank() && newId != "null") {
                    // Lier le code famille à ce nouvel ID dans KeyVal
                    val updateUrl = "$KEYVALUE_URL/UpdateValue/$APP_KEY/$cleanCode/$newId"
                    val dummyBody = "{}".toRequestBody(JSON_MEDIA_TYPE)
                    val updateRequest = Request.Builder()
                        .url(updateUrl)
                        .header("User-Agent", "YouTubePrivate-Android/1.0")
                        .post(dummyBody)
                        .build()
                    httpClient.newCall(updateRequest).execute()

                    cachedObjectId = newId
                    Log.d(TAG, "Nouvel ID cloud créé: $newId pour le code: $cleanCode")
                    return newId
                }
            } else {
                Log.e(TAG, "Échec création REST API code: ${createResponse.code}")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur création nouvel ID", e)
        }
        return null
    }

    private suspend fun syncWithCloud(cleanCode: String): Boolean = withContext(Dispatchers.IO) {
        val objectId = getOrCreateObjectId(cleanCode)
        if (objectId == null) {
            _syncState.value = CloudSyncState.OFFLINE
            return@withContext false
        }

        try {
            val getUrl = "$REST_API_URL/$objectId"
            val request = Request.Builder()
                .url(getUrl)
                .header("User-Agent", "YouTubePrivate-Android/1.0")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()

            if (response.code == 404) {
                cachedObjectId = null
                val newId = createAndStoreNewObjectId(cleanCode)
                if (newId != null) {
                    val localVideos = videoDao.getAllVideosList()
                    return@withContext pushLocalVideosToCloudInternal(newId, cleanCode, localVideos)
                } else {
                    _syncState.value = CloudSyncState.OFFLINE
                    return@withContext false
                }
            }

            if (!response.isSuccessful) {
                _syncState.value = CloudSyncState.OFFLINE
                return@withContext false
            }

            val jsonString = response.body?.string()?.trim() ?: "{}"
            val rootObj = JSONObject(jsonString)
            val dataObj = rootObj.optJSONObject("data") ?: rootObj
            val jsonArray = dataObj.optJSONArray("videos") ?: JSONArray()

            // Synchronisation des profils
            val profilesArray = dataObj.optJSONArray("profiles")
            if (profilesArray != null && profileDao != null) {
                val remoteProfiles = mutableListOf<ProfileEntity>()
                for (p in 0 until profilesArray.length()) {
                    val pObj = profilesArray.getJSONObject(p)
                    val pName = pObj.optString("name")
                    if (pName.isNotBlank()) {
                        val pId = pObj.optLong("id", 0L)
                        if (!deletedProfileIds.contains(pId)) {
                            val entity = ProfileEntity(
                                id = pId,
                                name = pName,
                                avatarEmoji = pObj.optString("avatarEmoji", "👦"),
                                age = pObj.optInt("age", 5),
                                createdAt = pObj.optLong("createdAt", System.currentTimeMillis())
                            )
                            remoteProfiles.add(entity)
                        }
                    }
                }
                if (remoteProfiles.isNotEmpty()) {
                    profileDao.insertProfiles(remoteProfiles)
                }
            }

            // Si le cloud est encore vide et qu'on a des vidéos en local, les envoyer
            if (jsonArray.length() == 0) {
                val localVideos = videoDao.getAllVideosList()
                if (localVideos.isNotEmpty() || (profileDao?.getAllProfilesList()?.isNotEmpty() == true)) {
                    pushLocalVideosToCloudInternal(objectId, cleanCode, localVideos)
                }
                _syncState.value = CloudSyncState.CONNECTED
                return@withContext true
            }

            // Mettre à jour la base de données locale avec les vidéos distantes
            val remoteEntities = mutableListOf<VideoEntity>()
            val remoteYoutubeIds = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val youtubeId = obj.optString("youtubeId")
                if (youtubeId.isBlank() || deletedYoutubeIds.contains(youtubeId)) continue

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
            return@withContext true
        } catch (e: Throwable) {
            Log.w(TAG, "Erreur synchronisation cloud: ${e.message}")
            _syncState.value = CloudSyncState.OFFLINE
            return@withContext false
        }
    }

    fun pushAllVideosToCloud(familyCode: String) {
        val cleanCode = familyCode.replace(" ", "").trim()
        if (cleanCode.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                val objectId = getOrCreateObjectId(cleanCode)
                if (objectId != null) {
                    val localVideos = videoDao.getAllVideosList()
                    pushLocalVideosToCloudInternal(objectId, cleanCode, localVideos)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Erreur envoi vidéos cloud", e)
            }
        }
    }

    fun addOrUpdateVideoInCloud(familyCode: String, video: Video) {
        pushAllVideosToCloud(familyCode)
    }

    fun deleteVideoFromCloud(familyCode: String, youtubeId: String) {
        recordDeletedVideo(youtubeId)
        pushAllVideosToCloud(familyCode)
    }

    private suspend fun pushLocalVideosToCloudInternal(
        objectId: String,
        cleanCode: String,
        localVideos: List<VideoEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val videosArray = JSONArray()
            localVideos.forEachIndexed { index, video ->
                if (!deletedYoutubeIds.contains(video.youtubeId)) {
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
                    videosArray.put(obj)
                }
            }

            val profilesArray = JSONArray()
            if (profileDao != null) {
                val localProfiles = profileDao.getAllProfilesList()
                localProfiles.forEach { p ->
                    if (!deletedProfileIds.contains(p.id)) {
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
            }

            val rootObj = JSONObject().apply {
                put("name", "family_$cleanCode")
                put("data", JSONObject().apply {
                    put("videos", videosArray)
                    put("profiles", profilesArray)
                })
            }

            val requestBody = rootObj.toString().toRequestBody(JSON_MEDIA_TYPE)
            val putUrl = "$REST_API_URL/$objectId"
            val request = Request.Builder()
                .url(putUrl)
                .header("User-Agent", "YouTubePrivate-Android/1.0")
                .put(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Succès : ${localVideos.size} vidéos poussées au cloud (ID: $objectId)")
                _syncState.value = CloudSyncState.CONNECTED
                return@withContext true
            } else {
                Log.w(TAG, "Échec mise à jour cloud ID $objectId (Code: ${response.code})")
                _syncState.value = CloudSyncState.OFFLINE
                return@withContext false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception envoi local vers cloud ID $objectId", e)
            _syncState.value = CloudSyncState.OFFLINE
            return@withContext false
        }
    }
}
