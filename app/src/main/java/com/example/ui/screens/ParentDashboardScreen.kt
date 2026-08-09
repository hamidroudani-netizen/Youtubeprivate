package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.data.remote.CloudSyncState
import com.example.domain.model.ChildProfile
import com.example.domain.model.Video
import com.example.ui.viewmodel.ParentViewModel
import com.example.ui.viewmodel.VideoViewModel
import com.example.util.AppLanguage
import com.example.util.BiometricAuthHelper
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    videoViewModel: VideoViewModel,
    parentViewModel: ParentViewModel,
    onNavigateToAddVideo: () -> Unit,
    onNavigateToEditVideo: (Video) -> Unit,
    onExitParentMode: () -> Unit
) {
    val strings = LocalAppStrings.current
    val videos by videoViewModel.videos.collectAsState()
    val profiles by videoViewModel.profiles.collectAsState()
    val selectedProfileId by videoViewModel.selectedProfileId.collectAsState()
    val familyCode by videoViewModel.familyCode.collectAsState()
    val cloudSyncState by videoViewModel.cloudSyncState.collectAsState()
    val isBiometricEnabled by parentViewModel.isBiometricEnabled.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var videoToDelete by remember { mutableStateOf<Video?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showJoinFamilyDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBiometricHardwareAvailable = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.parentDashboardTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${videos.size} ${strings.totalVideosCard}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.testTag("change_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = strings.changePinBtn
                        )
                    }
                    IconButton(
                        onClick = onExitParentMode,
                        modifier = Modifier.testTag("exit_parent_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = strings.closeBtn
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = strings.parentTabDashboard) },
                    label = { Text(strings.parentTabDashboard, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = strings.parentTabVideos) },
                    label = { Text(strings.parentTabVideos, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.People, contentDescription = strings.parentTabProfiles) },
                    label = { Text(strings.parentTabProfiles, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.parentTabSettings) },
                    label = { Text(strings.parentTabSettings, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(
                    onClick = onNavigateToAddVideo,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("add_video_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.addBtn
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.addBtn,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (selectedTab) {
                // Tab 0: Tableau de Bord Overview
                0 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Quick Stats Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${videos.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                                        Text(strings.totalVideosCard, style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${profiles.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                                        Text(strings.parentTabProfiles, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Family Code & Sync Card
                        item {
                            FamilyCodeSyncCard(
                                familyCode = familyCode,
                                syncState = cloudSyncState,
                                onCopyCode = {
                                    clipboardManager.setText(AnnotatedString(familyCode))
                                    Toast.makeText(context, "Code Famille $familyCode copié !", Toast.LENGTH_SHORT).show()
                                },
                                onChangeOrJoinCode = { showJoinFamilyDialog = true },
                                onGenerateNewCode = {
                                    val newCode = videoViewModel.generateNewFamilyCode()
                                    Toast.makeText(context, "Nouveau Code Famille : $newCode", Toast.LENGTH_LONG).show()
                                }
                            )
                        }

                        // Quick Import Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = strings.quickAddTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToAddVideo,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(strings.tabSingleVideo, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }

                                        OutlinedButton(
                                            onClick = onNavigateToAddVideo,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Text(strings.importChannelBtn, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 1: Video Management List
                1 -> {
                    val filteredVideos = remember(videos, searchQuery) {
                        if (searchQuery.isBlank()) videos
                        else videos.filter { it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(strings.searchLibraryPlaceholder) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (filteredVideos.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isBlank()) strings.noVideosPrompt else "${strings.noSearchResults} \"$searchQuery\"",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            items(
                                items = filteredVideos,
                                key = { it.id }
                            ) { video ->
                                ParentVideoItemCard(
                                    video = video,
                                    onFavoriteToggle = { videoViewModel.toggleFavorite(video) },
                                    onEditClick = { onNavigateToEditVideo(video) },
                                    onDeleteClick = { videoToDelete = video },
                                    onMoveUp = { videoViewModel.moveVideoUp(video, videos) },
                                    onMoveDown = { videoViewModel.moveVideoDown(video, videos) }
                                )
                            }
                        }
                    }
                }

                // Tab 2: Profiles Management
                2 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            ParentProfilesManagementCard(
                                profiles = profiles,
                                selectedProfileId = selectedProfileId,
                                onSelectProfile = { profileId -> videoViewModel.selectProfile(profileId) },
                                onAddProfile = { name, emoji, age ->
                                    videoViewModel.addProfile(
                                        name = name,
                                        avatarEmoji = emoji,
                                        age = age,
                                        onSuccess = { Toast.makeText(context, "Profil \"$name\" créé avec succès !", Toast.LENGTH_SHORT).show() },
                                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                onEditProfile = { profile ->
                                    videoViewModel.updateProfile(
                                        profile = profile,
                                        onSuccess = { Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show() },
                                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                onDeleteProfile = { profile ->
                                    videoViewModel.deleteProfile(profile)
                                    Toast.makeText(context, "Profil supprimé.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Tab 3: App Settings (Language, Theme, Security, Sync, About)
                3 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            ParentSettingsSection(
                                parentViewModel = parentViewModel,
                                videoViewModel = videoViewModel,
                                isBiometricHardwareAvailable = isBiometricHardwareAvailable,
                                onChangePinClick = { showChangePinDialog = true },
                                onChangeOrJoinFamilyCode = { showJoinFamilyDialog = true },
                                onGenerateNewFamilyCode = {
                                    val newCode = videoViewModel.generateNewFamilyCode()
                                    Toast.makeText(context, "Nouveau Code Famille généré : $newCode", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    videoToDelete?.let { video ->
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            title = { Text(strings.deleteVideoConfirmTitle) },
            text = { Text("${strings.deleteVideoConfirmText} (\"${video.title}\")") },
            confirmButton = {
                TextButton(
                    onClick = {
                        videoViewModel.deleteVideo(video)
                        videoToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(strings.deleteVideo, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        ChangePinDialog(
            parentViewModel = parentViewModel,
            onDismiss = { showChangePinDialog = false }
        )
    }

    // Join / Change Family Code Dialog
    if (showJoinFamilyDialog) {
        JoinFamilyCodeDialog(
            currentCode = familyCode,
            onConfirmCode = { newCode ->
                val ok = videoViewModel.updateFamilyCode(newCode)
                if (ok) {
                    Toast.makeText(context, "Connecté au Code Famille $newCode !", Toast.LENGTH_SHORT).show()
                    showJoinFamilyDialog = false
                } else {
                    Toast.makeText(context, "Code invalide. Veuillez entrer 6 chiffres.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showJoinFamilyDialog = false }
        )
    }
}

@Composable
fun ParentSettingsSection(
    parentViewModel: ParentViewModel,
    videoViewModel: VideoViewModel,
    isBiometricHardwareAvailable: Boolean,
    onChangePinClick: () -> Unit,
    onChangeOrJoinFamilyCode: () -> Unit,
    onGenerateNewFamilyCode: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLangCode by parentViewModel.appLanguage.collectAsState()
    val currentThemeSetting by parentViewModel.appTheme.collectAsState()
    val isBiometricEnabled by parentViewModel.isBiometricEnabled.collectAsState()
    val familyCode by videoViewModel.familyCode.collectAsState()
    val syncState by videoViewModel.cloudSyncState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Language Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        Triple("fr", "🇫🇷 Français", "Français"),
                        Triple("en", "🇬🇧 English", "English"),
                        Triple("ar", "🇸🇦 العربية", "العربية")
                    )

                    languages.forEach { (code, label, _) ->
                        val isSelected = currentLangCode == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { parentViewModel.setAppLanguage(code) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Theme Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.themeLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        Pair("light", strings.themeLight),
                        Pair("dark", strings.themeDark),
                        Pair("system", strings.themeSystem)
                    )

                    themes.forEach { (themeKey, label) ->
                        val isSelected = currentThemeSetting == themeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { parentViewModel.setAppTheme(themeKey) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Security Card
        ParentSecurityCard(
            isBiometricEnabled = isBiometricEnabled,
            isBiometricHardwareAvailable = isBiometricHardwareAvailable,
            onToggleBiometric = { enabled -> parentViewModel.setBiometricEnabled(enabled) },
            onChangePinClick = onChangePinClick
        )

        // 4. Family Cloud Sync Card
        FamilyCodeSyncCard(
            familyCode = familyCode,
            syncState = syncState,
            onCopyCode = {
                clipboardManager.setText(AnnotatedString(familyCode))
                Toast.makeText(context, "Code $familyCode copié", Toast.LENGTH_SHORT).show()
            },
            onChangeOrJoinCode = onChangeOrJoinFamilyCode,
            onGenerateNewCode = onGenerateNewFamilyCode
        )

        // 5. About Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "${strings.appName} v2.0.0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = strings.appAboutDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyCodeSyncCard(
    familyCode: String,
    syncState: CloudSyncState,
    onCopyCode: () -> Unit,
    onChangeOrJoinCode: () -> Unit,
    onGenerateNewCode: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top Row: Title & Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.familyCodeCardTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cloud status pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (syncState) {
                        CloudSyncState.CONNECTED -> Color(0xFFE8F5E9)
                        CloudSyncState.SYNCING -> Color(0xFFFFF8E1)
                        else -> Color(0xFFECEFF1)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (syncState) {
                                CloudSyncState.CONNECTED -> Icons.Default.CloudDone
                                CloudSyncState.SYNCING -> Icons.Default.Sync
                                else -> Icons.Default.CloudOff
                            },
                            contentDescription = null,
                            tint = when (syncState) {
                                CloudSyncState.CONNECTED -> Color(0xFF2E7D32)
                                CloudSyncState.SYNCING -> Color(0xFFF57F17)
                                else -> Color(0xFF546E7A)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (syncState) {
                                CloudSyncState.CONNECTED -> strings.cloudStatusOnline
                                CloudSyncState.SYNCING -> "Sync..."
                                else -> strings.cloudStatusOffline
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (syncState) {
                                CloudSyncState.CONNECTED -> Color(0xFF2E7D32)
                                CloudSyncState.SYNCING -> Color(0xFFF57F17)
                                else -> Color(0xFF546E7A)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6-digit Code Display Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.familyCodeCardTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = familyCode.chunked(3).joinToString(" "),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onCopyCode,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = strings.copyCodeBtn,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.familyCodeCardDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onChangeOrJoinCode,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.joinCodeBtn, style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onGenerateNewCode,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.newCodeBtn, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun JoinFamilyCodeDialog(
    currentCode: String,
    onConfirmCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var inputCode by remember { mutableStateOf(currentCode) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.joinCodeBtn,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = strings.familyCodeCardDesc,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            inputCode = newValue
                            errorMsg = ""
                        }
                    },
                    label = { Text(strings.familyCodeCardTitle) },
                    placeholder = { Text("482910") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMsg.isNotEmpty(),
                    supportingText = {
                        if (errorMsg.isNotEmpty()) {
                            Text(errorMsg, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputCode.length == 6 && inputCode.all { it.isDigit() }) {
                        onConfirmCode(inputCode)
                    } else {
                        errorMsg = "Code error"
                    }
                }
            ) {
                Text(strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn)
            }
        }
    )
}

@Composable
fun ParentVideoItemCard(
    video: Video,
    onFavoriteToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayCategory = when (video.category) {
                        "Dessins animés" -> strings.categoryCartoons
                        "Musique", "Chansons", "Comptines" -> strings.categoryMusic
                        "Éducatif" -> strings.categoryEducational
                        "Humour" -> strings.categoryHumor
                        "Tous" -> strings.categoryAll
                        "Favoris", "⭐ Favoris" -> strings.categoryFavorites
                        else -> video.category
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = displayCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${video.ageMin}-${video.ageMax} ${strings.yearsOld}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (video.favorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = strings.tabFavorites,
                        tint = if (video.favorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.editVideo,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.deleteVideo,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePinDialog(
    parentViewModel: ParentViewModel,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.changePinBtn) },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 4) oldPin = it },
                    label = { Text(strings.pinLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text(strings.pinLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parentViewModel.updatePin(
                        oldPin = oldPin,
                        newPin = newPin,
                        onSuccess = onDismiss,
                        onError = { errorMessage = it }
                    )
                }
            ) {
                Text(strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn)
            }
        }
    )
}

@Composable
fun ParentSecurityCard(
    isBiometricEnabled: Boolean,
    isBiometricHardwareAvailable: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onChangePinClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.sectionSecurity,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = strings.pinDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // PIN Code Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = strings.pinLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = strings.pinDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onChangePinClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.changePinBtn,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }

            // Biometric Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = if (isBiometricHardwareAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.biometricLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = strings.biometricDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }
        }
    }
}

@Composable
fun ParentProfilesManagementCard(
    profiles: List<ChildProfile>,
    selectedProfileId: Long = 0L,
    onSelectProfile: (Long) -> Unit = {},
    onAddProfile: (name: String, emoji: String, age: Int) -> Unit,
    onEditProfile: (ChildProfile) -> Unit,
    onDeleteProfile: (ChildProfile) -> Unit
) {
    val strings = LocalAppStrings.current
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<ChildProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<ChildProfile?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.profilesTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.profilesSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prominent full-width button to add new profile
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_profile_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.addProfileBtn,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Active Profile Selection for this device (Parent controlled)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📱 ${strings.activeProfileDeviceTitle} :",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.activeProfileDeviceDesc,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isSelected = selectedProfileId == 0L
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectProfile(0L) },
                                label = {
                                    Text(
                                        text = "🌈 ${strings.allProfilesLabel}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }

                        items(profiles, key = { it.id }) { profile ->
                            val isSelected = selectedProfileId == profile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectProfile(profile.id) },
                                label = {
                                    Text(
                                        text = "${profile.avatarEmoji} ${profile.name}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            if (profiles.isNotEmpty()) {
                Text(
                    text = strings.profilesTitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    profiles.forEach { profile ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.avatarEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${profile.age} ${strings.yearsOld}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { profileToEdit = profile },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = strings.editProfileTitle,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { profileToDelete = profile },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = strings.deleteVideo,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
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

    // Add Profile Dialog
    if (showAddDialog) {
        ChildProfileDialog(
            existingProfile = null,
            onConfirm = { name, emoji, age ->
                onAddProfile(name, emoji, age)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Profile Dialog
    profileToEdit?.let { profile ->
        ChildProfileDialog(
            existingProfile = profile,
            onConfirm = { name, emoji, age ->
                onEditProfile(profile.copy(name = name, avatarEmoji = emoji, age = age))
                profileToEdit = null
            },
            onDismiss = { profileToEdit = null }
        )
    }

    // Delete Profile Dialog
    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("${strings.deleteVideo} \"${profile.name}\" ?") },
            text = { Text(strings.deleteVideoConfirmText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profile)
                        profileToDelete = null
                    }
                ) {
                    Text(strings.deleteVideo, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }
}

@Composable
fun ChildProfileDialog(
    existingProfile: ChildProfile?,
    onConfirm: (name: String, emoji: String, age: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(existingProfile?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(existingProfile?.avatarEmoji ?: "👦") }
    var ageStr by remember { mutableStateOf((existingProfile?.age ?: 5).toString()) }
    var errorMsg by remember { mutableStateOf("") }

    val availableEmojis = listOf("👦", "👧", "🦁", "🦄", "🚀", "🎨", "⚽", "🐱", "🐶", "⭐")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingProfile == null) strings.createProfileTitle else strings.editProfileTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.childNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = strings.chooseAvatarEmoji,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    availableEmojis.take(5).forEach { emoji ->
                        Surface(
                            onClick = { selectedEmoji = emoji },
                            shape = CircleShape,
                            color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    availableEmojis.drop(5).forEach { emoji ->
                        Surface(
                            onClick = { selectedEmoji = emoji },
                            shape = CircleShape,
                            color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) ageStr = it },
                    label = { Text(strings.childAgeLabel) },
                    placeholder = { Text("5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = strings.childNameLabel
                    } else {
                        val ageVal = ageStr.toIntOrNull() ?: 5
                        onConfirm(name.trim(), selectedEmoji, ageVal)
                    }
                }
            ) {
                Text(if (existingProfile == null) strings.addBtn else strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn)
            }
        }
    )
}

