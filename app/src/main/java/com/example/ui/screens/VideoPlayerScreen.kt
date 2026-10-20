package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.components.YouTubeDiagnosticAnalyzer
import com.example.ui.components.YouTubeDiagnosticDialog
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.LocalAppStrings
import com.example.util.ShareHelper

private class YouTubeDiagnosticJsInterface(
    private val onErrorCode: (Int) -> Unit,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun onPlayerError(code: Int) {
        onErrorCode(code)
    }

    @JavascriptInterface
    fun onPlayerReady() {
        onReady()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    youtubeId: String,
    videoTitle: String,
    onBackClick: () -> Unit,
    onProgressUpdate: ((positionSeconds: Long, durationSeconds: Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val packageName = context.packageName
    var isLoading by remember { mutableStateOf(true) }
    var isRendererCrashed by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var lastErrorCode by remember { mutableStateOf<Int?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }

    // Mettre en pause le lecteur lors de la mise en arrière-plan sans détruire prématurément la vue attachée
    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.onPause()
            } catch (_: Exception) {}
        }
    }

    val htmlContent = remember(youtubeId, packageName) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; background-color: #000000; }
                html, body { width: 100%; height: 100%; overflow: hidden; background-color: #000000; }
                .iframe-container { position: relative; width: 100%; height: 100%; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <div class="iframe-container">
                <iframe 
                    id="yt-player"
                    src="https://www.youtube.com/embed/$youtubeId?playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=https://$packageName" 
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                    allowfullscreen>
                </iframe>
            </div>
            <script>
                window.addEventListener('message', function(event) {
                    try {
                        var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                        if (data && data.event === 'onError' && window.AndroidDiagnostic) {
                            window.AndroidDiagnostic.onPlayerError(parseInt(data.info || -1));
                        } else if (data && data.info && data.info.errorCode && window.AndroidDiagnostic) {
                            window.AndroidDiagnostic.onPlayerError(parseInt(data.info.errorCode));
                        } else if (data && data.event === 'initialDelivery' && window.AndroidDiagnostic) {
                            window.AndroidDiagnostic.onPlayerReady();
                        }
                    } catch(e) {}
                });
            </script>
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
        if (!isRendererCrashed) {
            androidx.compose.runtime.key(reloadKey) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mediaPlaybackRequiresUserGesture = false
                            }

                            webChromeClient = WebChromeClient()

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }

                                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                    try {
                                        (view?.parent as? ViewGroup)?.removeView(view)
                                        view?.destroy()
                                    } catch (_: Exception) {}
                                    webViewRef = null
                                    isRendererCrashed = true
                                    isLoading = false
                                    return true
                                }
                            }
                            addJavascriptInterface(
                                YouTubeDiagnosticJsInterface(
                                    onErrorCode = { code ->
                                        lastErrorCode = code
                                        isLoading = false
                                    },
                                    onReady = {
                                        isPlayerReady = true
                                        isLoading = false
                                    }
                                ),
                                "AndroidDiagnostic"
                            )
                            loadDataWithBaseURL("https://$packageName", htmlContent, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    },
                    onRelease = { webView ->
                        try {
                            webView.stopLoading()
                            webView.webChromeClient = null
                            (webView.parent as? ViewGroup)?.removeView(webView)
                            webView.removeAllViews()
                            webView.destroy()
                        } catch (_: Exception) {}
                        if (webViewRef === webView) {
                            webViewRef = null
                        }
                    },
                    update = { /* Configuration initiale stable */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Écran de secours élégant si le moteur WebView subit un arrêt inattendu sur l'émulateur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = videoTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Le lecteur interne s'est réinitialisé.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isRendererCrashed = false
                            isLoading = true
                            reloadKey++
                        },
                        modifier = Modifier.testTag("player_retry_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Relancer la vidéo")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$youtubeId")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.testTag("player_external_button")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ouvrir dans YouTube")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDiagnosticDialog = true },
                        modifier = Modifier.testTag("player_fallback_diagnostic_button")
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Diagnostic du lecteur")
                    }
                }
            }
        }

        // Indicateur de chargement
        AnimatedVisibility(
            visible = isLoading && !isRendererCrashed,
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

        // Barre supérieure avec bouton retour, titre et actions
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
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

                // Bouton Diagnostic
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_diagnostic_button")
                ) {
                    Surface(
                        onClick = { showDiagnosticDialog = true },
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Diagnostic du lecteur",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Bouton Ouvrir sur YouTube
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_open_youtube_button")
                ) {
                    Surface(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$youtubeId")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Ouvrir dans YouTube",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Bouton Partager
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

        // Bannière flottante si une erreur de lecteur YouTube est détectée
        AnimatedVisibility(
            visible = lastErrorCode != null && !showDiagnosticDialog,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Problème de lecture (Code $lastErrorCode)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (lastErrorCode == 150 || lastErrorCode == 101) "Intégration restreinte par l'auteur" else "Consultez le diagnostic du lecteur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showDiagnosticDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.testTag("player_error_banner_diagnostic_button")
                    ) {
                        Text("Détails")
                    }
                }
            }
        }

        // Boîte de dialogue du diagnostic YouTube
        if (showDiagnosticDialog) {
            val report = YouTubeDiagnosticAnalyzer.analyze(
                youtubeId = youtubeId,
                packageName = packageName,
                errorCode = lastErrorCode,
                isRendererCrashed = isRendererCrashed,
                isPlayerReady = isPlayerReady
            )
            YouTubeDiagnosticDialog(
                report = report,
                onDismiss = { showDiagnosticDialog = false },
                onOpenExternal = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=$youtubeId")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}
