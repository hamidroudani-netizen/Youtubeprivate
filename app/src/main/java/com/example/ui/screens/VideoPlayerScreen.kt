package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.LocalAppStrings
import com.example.util.ShareHelper

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    youtubeId: String,
    videoTitle: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    val htmlContent = remember(youtubeId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; background-color: #000000; }
                html, body { width: 100%; height: 100%; overflow: hidden; background-color: #000000; display: flex; justify-content: center; align-items: center; }
                .iframe-container { position: relative; width: 100%; height: 100%; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <div class="iframe-container">
                <iframe 
                    src="https://www.youtube-nocookie.com/embed/$youtubeId?autoplay=1&controls=1&rel=0&modestbranding=1&iv_load_policy=3&fs=1&playsinline=1" 
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                    allowfullscreen>
                </iframe>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        // Native WebView embedding the embedded player
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        // Strictly prevent opening YouTube website, external channels, or ads!
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: ""
                            // Only allow internal embed player URL
                            return if (url.contains("youtube-nocookie.com/embed") || url.startsWith("data:")) {
                                false
                            } else {
                                true // Block outward navigation
                            }
                        }
                    }
                    loadDataWithBaseURL("https://www.youtube-nocookie.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                // WebView updated if necessary
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
        }

        // Top Overlay Bar with Child-Friendly Large Back Button & Video Title
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Child Back Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("player_back_button")
                ) {
                    Surface(
                        onClick = onBackClick,
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val strings = LocalAppStrings.current
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.closeBtn,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = videoTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Share Button for kids/parents
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_share_button")
                ) {
                    Surface(
                        onClick = {
                            ShareHelper.shareVideoById(context, youtubeId, videoTitle)
                        },
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partager la vidéo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
