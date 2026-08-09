package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ParentPreferences
import com.example.data.remote.CloudSyncState
import com.example.data.repository.VideoRepository
import com.example.domain.model.Video
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository
    private val parentPreferences: ParentPreferences = ParentPreferences(application)

    val selectedCategory = MutableStateFlow("Tous")
    val selectedProfileId = MutableStateFlow(parentPreferences.getActiveProfileId())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _familyCode = MutableStateFlow(parentPreferences.getFamilyCode())
    val familyCode: StateFlow<String> = _familyCode.asStateFlow()

    val cloudSyncState: StateFlow<CloudSyncState>

    val categories = listOf("Tous", "⭐ Favoris", "Dessins animés", "Comptines", "Éducatif", "Chansons")

    val profiles: StateFlow<List<com.example.domain.model.ChildProfile>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VideoRepository(
            application,
            database.videoDao(),
            database.profileDao(),
            parentPreferences
        )
        cloudSyncState = repository.cloudSyncState
        profiles = repository.allProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        // Start listening to real-time Cloud Firestore updates for the current Family Code
        repository.startCloudSync()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val videos: StateFlow<List<Video>> = combine(
        selectedCategory,
        selectedProfileId,
        _searchQuery
    ) { category, profileId, query ->
        Triple(category, profileId, query)
    }.flatMapLatest { (category, profileId, query) ->
        val flow = when (category) {
            "Tous" -> repository.allVideos
            "⭐ Favoris" -> repository.favoriteVideos
            else -> repository.getVideosByCategory(category)
        }
        flow
    }.combine(selectedProfileId) { videoList, profileId ->
        if (profileId == 0L) {
            videoList
        } else {
            videoList.filter { it.targetProfileId == 0L || it.targetProfileId == profileId }
        }
    }.combine(_searchQuery) { videoList, query ->
        if (query.isBlank()) {
            videoList
        } else {
            videoList.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectProfile(profileId: Long) {
        selectedProfileId.value = profileId
        parentPreferences.setActiveProfileId(profileId)
    }

    fun addProfile(name: String, avatarEmoji: String = "👦", age: Int = 5, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (name.isBlank()) {
            onError("Le nom du profil ne peut pas être vide.")
            return
        }
        viewModelScope.launch {
            try {
                repository.insertProfile(com.example.domain.model.ChildProfile(name = name.trim(), avatarEmoji = avatarEmoji, age = age))
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de la création du profil.")
            }
        }
    }

    fun updateProfile(profile: com.example.domain.model.ChildProfile, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (profile.name.isBlank()) {
            onError("Le nom du profil ne peut pas être vide.")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de la mise à jour du profil.")
            }
        }
    }

    fun deleteProfile(profile: com.example.domain.model.ChildProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (selectedProfileId.value == profile.id) {
                selectedProfileId.value = 0L
            }
        }
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFamilyCode(newCode: String): Boolean {
        val success = repository.updateFamilyCode(newCode)
        if (success) {
            _familyCode.value = newCode
        }
        return success
    }

    fun generateNewFamilyCode(): String {
        val newCode = repository.generateNewFamilyCode()
        _familyCode.value = newCode
        return newCode
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, video.favorite)
        }
    }

    fun markAsPlayed(video: Video) {
        viewModelScope.launch {
            repository.updateLastPlayed(video.id)
        }
    }

    val isAnalyzingChannel = MutableStateFlow(false)
    val channelPreviewResult = MutableStateFlow<com.example.data.remote.ImportedChannelResult?>(null)
    val channelImportError = MutableStateFlow<String?>(null)

    fun analyzeChannelOrPlaylist(inputUrlOrHandle: String) {
        if (inputUrlOrHandle.isBlank()) {
            channelImportError.value = "Veuillez entrer une URL de chaîne ou un nom de chaîne (ex: @cocomelon)."
            return
        }

        isAnalyzingChannel.value = true
        channelImportError.value = null
        channelPreviewResult.value = null

        viewModelScope.launch {
            val result = repository.fetchChannelVideos(inputUrlOrHandle)
            isAnalyzingChannel.value = false
            result.fold(
                onSuccess = { channelResult ->
                    channelPreviewResult.value = channelResult
                },
                onFailure = { error ->
                    channelImportError.value = error.localizedMessage ?: "Erreur lors de l'analyse de la chaîne."
                }
            )
        }
    }

    fun clearChannelPreview() {
        channelPreviewResult.value = null
        channelImportError.value = null
        isAnalyzingChannel.value = false
    }

    fun importSelectedChannelVideos(
        itemsToImport: List<com.example.data.remote.ImportedVideoItem>,
        category: String,
        targetProfileId: Long = 0L,
        ageMin: Int = 3,
        ageMax: Int = 10,
        onSuccess: (importedCount: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (itemsToImport.isEmpty()) {
            onError("Aucune vidéo sélectionnée pour l'importation.")
            return
        }

        val cleanCategory = if (category.startsWith("⭐")) "Général" else category
        val now = System.currentTimeMillis()

        val videoList = itemsToImport.mapIndexed { index, item ->
            Video(
                youtubeId = item.youtubeId,
                title = item.title.ifBlank { "Vidéo ${item.youtubeId}" },
                thumbnailUrl = item.thumbnailUrl,
                category = cleanCategory,
                description = item.description,
                targetProfileId = targetProfileId,
                ageMin = ageMin,
                ageMax = ageMax,
                order = index,
                createdAt = now + index
            )
        }

        viewModelScope.launch {
            try {
                repository.importMultipleVideos(videoList)
                clearChannelPreview()
                onSuccess(videoList.size)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de l'importation de la chaîne.")
            }
        }
    }

    fun addVideo(
        youtubeUrlOrId: String,
        customTitle: String,
        category: String,
        targetProfileId: Long = 0L,
        description: String = "",
        ageMin: Int = 3,
        ageMax: Int = 10,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val extractedId = VideoRepository.extractYoutubeId(youtubeUrlOrId)
        if (extractedId == null) {
            onError("L'URL YouTube n'est pas valide. Exemple: https://www.youtube.com/watch?v=...")
            return
        }

        if (customTitle.isBlank()) {
            onError("Veuillez saisir un titre pour la vidéo.")
            return
        }

        val thumbnailUrl = VideoRepository.generateThumbnailUrl(extractedId)

        val newVideo = Video(
            youtubeId = extractedId,
            title = customTitle.trim(),
            thumbnailUrl = thumbnailUrl,
            category = if (category.startsWith("⭐")) "Général" else category,
            description = description.trim(),
            targetProfileId = targetProfileId,
            ageMin = ageMin,
            ageMax = ageMax,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                repository.insertVideo(newVideo)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de l'ajout de la vidéo")
            }
        }
    }

    fun updateVideo(
        video: Video,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (video.title.isBlank()) {
            onError("Le titre ne peut pas être vide.")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateVideo(video)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de la mise à jour.")
            }
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            repository.deleteVideo(video)
        }
    }

    fun moveVideoUp(video: Video, currentList: List<Video>) {
        val index = currentList.indexOfFirst { it.id == video.id }
        if (index > 0) {
            val newList = currentList.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index - 1]
            newList[index - 1] = temp
            viewModelScope.launch {
                repository.updateVideoOrders(newList)
            }
        }
    }

    fun moveVideoDown(video: Video, currentList: List<Video>) {
        val index = currentList.indexOfFirst { it.id == video.id }
        if (index >= 0 && index < currentList.size - 1) {
            val newList = currentList.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index + 1]
            newList[index + 1] = temp
            viewModelScope.launch {
                repository.updateVideoOrders(newList)
            }
        }
    }
}
