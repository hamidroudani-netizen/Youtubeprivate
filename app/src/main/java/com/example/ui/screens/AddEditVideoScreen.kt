package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.ImportedVideoItem
import com.example.data.repository.VideoRepository
import com.example.domain.model.Video
import com.example.ui.viewmodel.VideoViewModel
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVideoScreen(
    videoViewModel: VideoViewModel,
    existingVideo: Video? = null,
    onNavigateBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Single Video, 1 = Entire Channel

    // Single video state
    var urlInput by remember { mutableStateOf(existingVideo?.let { "https://www.youtube.com/watch?v=${it.youtubeId}" } ?: "") }
    var extractedYoutubeId by remember { mutableStateOf(existingVideo?.youtubeId ?: "") }
    var titleInput by remember { mutableStateOf(existingVideo?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(existingVideo?.category ?: strings.categoryCartoons) }
    var descriptionInput by remember { mutableStateOf(existingVideo?.description ?: "") }
    var ageMin by remember { mutableStateOf(existingVideo?.ageMin ?: 3) }
    var ageMax by remember { mutableStateOf(existingVideo?.ageMax ?: 10) }
    var targetProfileId by remember { mutableStateOf(existingVideo?.targetProfileId ?: 0L) }

    val profiles by videoViewModel.profiles.collectAsState()

    // Channel state
    var channelInput by remember { mutableStateOf("") }
    val isAnalyzingChannel by videoViewModel.isAnalyzingChannel.collectAsState()
    val channelPreviewResult by videoViewModel.channelPreviewResult.collectAsState()
    val channelImportError by videoViewModel.channelImportError.collectAsState()
    val selectedChannelVideoIds = remember { mutableStateListOf<String>() }

    var categoryExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(strings.categoryCartoons, strings.categoryMusic, strings.categoryEducational, strings.categoryHumor, strings.categoryAll)

    // Automatically extract YouTube ID whenever urlInput changes for single video
    LaunchedEffect(urlInput) {
        val extracted = VideoRepository.extractYoutubeId(urlInput)
        if (extracted != null) {
            extractedYoutubeId = extracted
            errorMessage = null
        } else if (urlInput.isNotBlank()) {
            extractedYoutubeId = ""
        }
    }

    // Automatically select all videos when channel results are fetched
    LaunchedEffect(channelPreviewResult) {
        channelPreviewResult?.let { res ->
            selectedChannelVideoIds.clear()
            selectedChannelVideoIds.addAll(res.videos.map { it.youtubeId })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingVideo == null) strings.addContentTitle else strings.editVideoTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_video_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.closeBtn
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Tab Selector (Single video vs Full Channel)
            if (existingVideo == null) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                strings.tabSingleVideo,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                strings.tabEntireChannel,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (selectedTabIndex == 0 || existingVideo != null) {
                    // MODE 1: SINGLE VIDEO
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = strings.step1VideoLink,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    label = { Text(strings.pasteUrlLabel) },
                                    placeholder = { Text(strings.youtubeUrlHint) },
                                    singleLine = true,
                                    enabled = existingVideo == null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("youtube_url_input")
                                )

                                if (existingVideo == null) {
                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            clipboardManager.getText()?.let {
                                                urlInput = it.text
                                            }
                                        },
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                            )
                                            .size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = strings.pasteBtn,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Live Thumbnail Preview
                            if (extractedYoutubeId.isNotBlank()) {
                                val previewThumbUrl = VideoRepository.generateThumbnailUrl(extractedYoutubeId)

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.thumbnailPreviewLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(previewThumbUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            } else if (urlInput.isNotBlank()) {
                                Text(
                                    text = strings.invalidUrlFormat,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Single Video Metadata Details
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = strings.step2VideoInfo,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text(strings.customTitleLabel) },
                                placeholder = { Text(strings.customTitleHint) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("video_title_input")
                            )

                            // Category Selector
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.categoryLabel) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("video_category_dropdown")
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                selectedCategory = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Profile Selector
                            var profileExpanded by remember { mutableStateOf(false) }
                            val profileOptions = listOf(0L to "🌈 ${strings.videoAssignedToAll}") + profiles.map { it.id to "${it.avatarEmoji} ${it.name}" }
                            val currentProfileName = profileOptions.firstOrNull { it.first == targetProfileId }?.second ?: "🌈 ${strings.videoAssignedToAll}"

                            ExposedDropdownMenuBox(
                                expanded = profileExpanded,
                                onExpandedChange = { profileExpanded = !profileExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentProfileName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.assignProfileLabel) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = profileExpanded,
                                    onDismissRequest = { profileExpanded = false }
                                ) {
                                    profileOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.second) },
                                            onClick = {
                                                targetProfileId = option.first
                                                profileExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                label = { Text(strings.parentNoteLabel) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }

                    errorMessage?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Save / Submit Button for Single Video
                    Button(
                        onClick = {
                            if (existingVideo == null) {
                                videoViewModel.addVideo(
                                    youtubeUrlOrId = urlInput,
                                    customTitle = titleInput,
                                    category = selectedCategory,
                                    targetProfileId = targetProfileId,
                                    description = descriptionInput,
                                    ageMin = ageMin,
                                    ageMax = ageMax,
                                    onSuccess = onNavigateBack,
                                    onError = { err -> errorMessage = err }
                                )
                            } else {
                                videoViewModel.updateVideo(
                                    video = existingVideo.copy(
                                        title = titleInput.trim(),
                                        category = selectedCategory,
                                        targetProfileId = targetProfileId,
                                        description = descriptionInput.trim(),
                                        ageMin = ageMin,
                                        ageMax = ageMax
                                    ),
                                    onSuccess = onNavigateBack,
                                    onError = { err -> errorMessage = err }
                                )
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("save_video_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (existingVideo == null) strings.addToLibraryBtn else strings.saveChangesBtn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                } else {
                    // MODE 2: ENTIRE CHANNEL IMPORT
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = strings.channelInputTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = strings.channelInputDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = channelInput,
                                    onValueChange = { channelInput = it },
                                    label = { Text(strings.channelInputLabel) },
                                    placeholder = { Text(strings.channelInputHint) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        clipboardManager.getText()?.let {
                                            channelInput = it.text
                                        }
                                    },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        )
                                        .size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = strings.pasteBtn,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    videoViewModel.analyzeChannelOrPlaylist(channelInput)
                                },
                                enabled = !isAnalyzingChannel && channelInput.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isAnalyzingChannel) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(strings.analyzingChannelMsg)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.analyzeChannelBtn)
                                }
                            }
                        }
                    }

                    // Error message for channel analysis
                    channelImportError?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Preview of fetched channel videos
                    channelPreviewResult?.let { res ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "${strings.channelPrefix} ${res.channelTitle}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedChannelVideoIds.size} / ${res.videos.size} ${strings.videosSelectedCount}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )

                                    Text(
                                        text = if (selectedChannelVideoIds.size == res.videos.size) strings.deselectAll else strings.selectAll,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (selectedChannelVideoIds.size == res.videos.size) {
                                                    selectedChannelVideoIds.clear()
                                                } else {
                                                    selectedChannelVideoIds.clear()
                                                    selectedChannelVideoIds.addAll(res.videos.map { it.youtubeId })
                                                }
                                            }
                                            .padding(4.dp)
                                    )
                                }

                                // Category selector for imported channel
                                ExposedDropdownMenuBox(
                                    expanded = categoryExpanded,
                                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(strings.channelCategoryLabel) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = categoryExpanded,
                                        onDismissRequest = { categoryExpanded = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    selectedCategory = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // List of channel videos with checkable items
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    res.videos.forEach { item ->
                                        val isSelected = selectedChannelVideoIds.contains(item.youtubeId)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isSelected) {
                                                        selectedChannelVideoIds.remove(item.youtubeId)
                                                    } else {
                                                        selectedChannelVideoIds.add(item.youtubeId)
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        if (checked == true) {
                                                            selectedChannelVideoIds.add(item.youtubeId)
                                                        } else {
                                                            selectedChannelVideoIds.remove(item.youtubeId)
                                                        }
                                                    }
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp, 45.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.Black)
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(item.thumbnailUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Submit channel import
                                Button(
                                    onClick = {
                                        val selectedItems = res.videos.filter { selectedChannelVideoIds.contains(it.youtubeId) }
                                        videoViewModel.importSelectedChannelVideos(
                                            itemsToImport = selectedItems,
                                            category = selectedCategory,
                                            targetProfileId = targetProfileId,
                                            ageMin = ageMin,
                                            ageMax = ageMax,
                                            onSuccess = { count ->
                                                Toast.makeText(context, "$count ${strings.importSuccessToast}", Toast.LENGTH_LONG).show()
                                                onNavigateBack()
                                            },
                                            onError = { err -> errorMessage = err }
                                        )
                                    },
                                    enabled = selectedChannelVideoIds.isNotEmpty(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${strings.importVideosBtnPrefix} ${selectedChannelVideoIds.size} ${strings.videosSelectedCount}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
