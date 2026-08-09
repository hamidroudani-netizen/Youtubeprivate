package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.Video
import com.example.ui.components.PinEntryModal
import com.example.ui.screens.AddEditVideoScreen
import com.example.ui.screens.ChildHomeScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.YouTubePriveTheme
import com.example.ui.viewmodel.ParentViewModel
import com.example.ui.viewmodel.VideoViewModel
import com.example.util.BiometricAuthHelper

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings
import com.example.util.getAppStrings

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouTubePriveApp()
        }
    }
}

@Composable
fun YouTubePriveApp(
    videoViewModel: VideoViewModel = viewModel(),
    parentViewModel: ParentViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val navController = rememberNavController()

    val appLanguageCode by parentViewModel.appLanguage.collectAsState()
    val appThemeSetting by parentViewModel.appTheme.collectAsState()

    val appLanguage = remember(appLanguageCode) { AppLanguage.fromCode(appLanguageCode) }
    val appStrings = remember(appLanguageCode) { getAppStrings(appLanguageCode) }

    CompositionLocalProvider(
        LocalAppLanguage provides appLanguage,
        LocalAppStrings provides appStrings,
        LocalLayoutDirection provides appLanguage.layoutDirection
    ) {
        YouTubePriveTheme(appThemeSetting = appThemeSetting) {
            Surface(modifier = Modifier.fillMaxSize()) {
                YouTubePriveAppContent(
                    navController = navController,
                    activity = activity,
                    videoViewModel = videoViewModel,
                    parentViewModel = parentViewModel
                )
            }
        }
    }
}

@Composable
private fun YouTubePriveAppContent(
    navController: androidx.navigation.NavHostController,
    activity: FragmentActivity?,
    videoViewModel: VideoViewModel,
    parentViewModel: ParentViewModel
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isAuthenticated by parentViewModel.isAuthenticated.collectAsState()
    val showPinModal by parentViewModel.showPinModal.collectAsState()
    val pinInput by parentViewModel.pinInput.collectAsState()
    val pinError by parentViewModel.pinError.collectAsState()
    val isBiometricEnabled by parentViewModel.isBiometricEnabled.collectAsState()

    val isBiometricAvailable = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }

    val videos by videoViewModel.videos.collectAsState()

    // Automatically trigger biometric authentication when PIN modal opens if biometric is enabled
    LaunchedEffect(showPinModal) {
        if (showPinModal && isBiometricEnabled && isBiometricAvailable && activity != null) {
            BiometricAuthHelper.promptBiometric(
                activity = activity,
                title = strings.parentDashboardTitle,
                subtitle = strings.pinModalDefaultSubtitle,
                onSuccess = {
                    parentViewModel.onBiometricSuccess()
                    navController.navigate("parent_dashboard")
                },
                onError = { /* Fallback to PIN code if biometric fails or is cancelled */ }
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 1. Child Home Library
        composable("home") {
            ChildHomeScreen(
                viewModel = videoViewModel,
                onVideoClick = { video ->
                    val encodedTitle = Uri.encode(video.title)
                    navController.navigate("player/${video.youtubeId}/$encodedTitle")
                },
                onOpenParentSpace = {
                    if (isAuthenticated) {
                        navController.navigate("parent_dashboard")
                    } else {
                        parentViewModel.openPinModal()
                    }
                }
            )
        }

        // 2. Kid Video Player
        composable(
            route = "player/{youtubeId}/{title}",
            arguments = listOf(
                navArgument("youtubeId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val youtubeId = backStackEntry.arguments?.getString("youtubeId") ?: ""
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "Vidéo")

            VideoPlayerScreen(
                youtubeId = youtubeId,
                videoTitle = title,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Parent Management Dashboard
        composable("parent_dashboard") {
            ParentDashboardScreen(
                videoViewModel = videoViewModel,
                parentViewModel = parentViewModel,
                onNavigateToAddVideo = { navController.navigate("add_video") },
                onNavigateToEditVideo = { video ->
                    navController.navigate("edit_video/${video.id}")
                },
                onExitParentMode = {
                    parentViewModel.logout()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 4. Add Video Form
        composable("add_video") {
            AddEditVideoScreen(
                videoViewModel = videoViewModel,
                existingVideo = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Edit Video Form
        composable(
            route = "edit_video/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            val existingVideo = videos.find { it.id == videoId }

            AddEditVideoScreen(
                videoViewModel = videoViewModel,
                existingVideo = existingVideo,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    // Parent PIN Auth Modal Dialog
    if (showPinModal) {
        PinEntryModal(
            pinInput = pinInput,
            pinError = pinError,
            isBiometricAvailable = isBiometricAvailable && isBiometricEnabled,
            onBiometricClick = {
                if (activity != null) {
                    BiometricAuthHelper.promptBiometric(
                        activity = activity,
                        title = strings.parentDashboardTitle,
                        subtitle = strings.pinModalDefaultSubtitle,
                        onSuccess = {
                            parentViewModel.onBiometricSuccess()
                            navController.navigate("parent_dashboard")
                        },
                        onError = { /* Handled in prompt */ }
                    )
                }
            },
            onDigitClick = { digit ->
                parentViewModel.appendPinDigit(digit)
                if (parentViewModel.isAuthenticated.value) {
                    navController.navigate("parent_dashboard")
                }
            },
            onDeleteClick = { parentViewModel.deletePinDigit() },
            onDismiss = { parentViewModel.closePinModal() }
        )
    }
}
