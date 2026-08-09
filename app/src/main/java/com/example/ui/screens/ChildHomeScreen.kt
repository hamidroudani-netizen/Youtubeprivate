package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Video
import com.example.ui.components.CategoryChip
import com.example.ui.components.VideoCard
import com.example.ui.viewmodel.VideoViewModel

import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHomeScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit,
    onOpenParentSpace: () -> Unit
) {
    val strings = LocalAppStrings.current
    val videos by viewModel.videos.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val familyCode by viewModel.familyCode.collectAsState()
    val formattedFamilyCode = familyCode.chunked(3).joinToString(" ")

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title / Logo Area
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.appName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${strings.appTagline} • Code: $formattedFamilyCode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Parent button (Fully clickable surface with clear touch target)
                        Surface(
                            onClick = onOpenParentSpace,
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("parent_space_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = strings.parentButton,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = strings.parentButton,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Active profile badge selected by parent
                    val activeProfile = profiles.firstOrNull { it.id == selectedProfileId }
                    val activeProfileDisplay = if (selectedProfileId == 0L || activeProfile == null) {
                        "🌈 ${strings.allProfilesLabel}"
                    } else {
                        "${activeProfile.avatarEmoji} ${activeProfile.name}"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.activeSpacePrefix,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = activeProfileDisplay,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }

                    // Horizontal Category Chips Row
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(viewModel.categories) { category ->
                            val translatedCategory = when (category) {
                                "Tous" -> strings.categoryAll
                                "Favoris" -> strings.categoryFavorites
                                "Dessins animés" -> strings.categoryCartoons
                                "Musique" -> strings.categoryMusic
                                "Éducatif" -> strings.categoryEducational
                                "Humour" -> strings.categoryHumor
                                else -> category
                            }
                            CategoryChip(
                                category = translatedCategory,
                                isSelected = selectedCategory == category,
                                onSelect = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val columns = when {
                maxWidth >= 840.dp -> 4
                maxWidth >= 600.dp -> 3
                else -> 2
            }

            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = strings.noVideosFound,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = videos,
                        key = { it.id }
                    ) { video ->
                        VideoCard(
                            video = video,
                            onClick = {
                                viewModel.markAsPlayed(video)
                                onVideoClick(video)
                            },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(video)
                            }
                        )
                    }
                }
            }
        }
    }
}
