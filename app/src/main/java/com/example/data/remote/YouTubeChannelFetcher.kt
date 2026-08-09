package com.example.data.remote

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ImportedVideoItem(
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val description: String = ""
)

data class ImportedChannelResult(
    val channelTitle: String,
    val videos: List<ImportedVideoItem>
)

class YouTubeChannelFetcher {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private companion object {
        private const val TAG = "YouTubeChannelFetcher"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    suspend fun fetchChannelVideos(input: String): Result<ImportedChannelResult> = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(Exception("Veuillez saisir un lien ou un nom de chaîne YouTube (ex: @cocomelon ou un lien de chaîne)."))
        }

        try {
            var channelId: String? = null
            var channelTitle = "Chaîne YouTube"

            // 1. Direct Channel ID check (UC...)
            if (trimmed.startsWith("UC") && trimmed.length == 24) {
                channelId = trimmed
            }

            // 2. Extract channel ID from channel URL (e.g., youtube.com/channel/UC...)
            if (channelId == null) {
                val channelPattern = Pattern.compile("channel/(UC[a-zA-Z0-9_-]{22})", Pattern.CASE_INSENSITIVE)
                val matcher = channelPattern.matcher(trimmed)
                if (matcher.find()) {
                    channelId = matcher.group(1)
                }
            }

            // 3. Handle or custom URL (e.g. @cocomelon, youtube.com/@peppapig)
            if (channelId == null) {
                val targetUrl = buildChannelUrl(trimmed)
                Log.d(TAG, "Fetching HTML from channel URL: $targetUrl")

                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""

                    // Extract channel_id from HTML
                    val cidPattern = Pattern.compile("(?:channel_id=|\"channelId\":\"|\"externalChannelId\":\"|\"canonicalBaseUrl\":\"/channel/)(UC[a-zA-Z0-9_-]{22})")
                    val cidMatcher = cidPattern.matcher(html)
                    if (cidMatcher.find()) {
                        channelId = cidMatcher.group(1)
                        Log.d(TAG, "Resolved Channel ID from HTML: $channelId")
                    }

                    // Extract title from HTML
                    val titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE)
                    val titleMatcher = titlePattern.matcher(html)
                    if (titleMatcher.find()) {
                        val rawTitle = titleMatcher.group(1) ?: ""
                        channelTitle = rawTitle
                            .replace(" - YouTube", "", ignoreCase = true)
                            .replace(" - Vidéos", "", ignoreCase = true)
                            .replace(" - Videos", "", ignoreCase = true)
                            .trim()
                    }
                }
            }

            // 4. Fetch RSS Feed if Channel ID is found
            if (channelId != null) {
                val rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
                Log.d(TAG, "Fetching RSS feed: $rssUrl")

                val rssRequest = Request.Builder()
                    .url(rssUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()

                val rssResponse = httpClient.newCall(rssRequest).execute()
                if (rssResponse.isSuccessful) {
                    val xmlString = rssResponse.body?.string() ?: ""
                    if (xmlString.isNotBlank()) {
                        val parsed = parseRssXml(xmlString)
                        if (parsed.videos.isNotEmpty()) {
                            val finalTitle = if (parsed.channelTitle.isNotBlank() && parsed.channelTitle != "Chaîne YouTube") {
                                parsed.channelTitle
                            } else {
                                channelTitle
                            }
                            return@withContext Result.success(ImportedChannelResult(finalTitle, parsed.videos))
                        }
                    }
                }
            }

            // 5. Fallback HTML Video Extraction if RSS feed gave no videos
            val targetUrl = buildChannelUrl(trimmed)
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                val extracted = extractVideosFromHtml(html)
                if (extracted.isNotEmpty()) {
                    return@withContext Result.success(ImportedChannelResult(channelTitle, extracted))
                }
            }

            Result.failure(Exception("Impossible de trouver des vidéos pour cette chaîne. Vérifiez l'identifiant ou l'URL (ex: @cocomelon)."))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching channel videos", e)
            Result.failure(Exception("Erreur de connexion : ${e.localizedMessage ?: "Vérifiez votre connexion internet."}"))
        }
    }

    private fun buildChannelUrl(input: String): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> {
                if (!input.contains("/videos") && !input.contains("/featured")) {
                    input.trimEnd('/') + "/videos"
                } else {
                    input
                }
            }
            input.startsWith("@") -> "https://www.youtube.com/$input/videos"
            else -> "https://www.youtube.com/@$input/videos"
        }
    }

    private fun parseRssXml(xmlString: String): ImportedChannelResult {
        val videos = mutableListOf<ImportedVideoItem>()
        var channelTitle = "Chaîne YouTube"

        try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            var inEntry = false
            var currentVideoId = ""
            var currentTitle = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("entry", ignoreCase = true)) {
                            inEntry = true
                            currentVideoId = ""
                            currentTitle = ""
                        } else if (!inEntry && tagName.equals("title", ignoreCase = true)) {
                            channelTitle = parser.nextText().trim()
                        } else if (inEntry) {
                            if (tagName.equals("yt:videoId", ignoreCase = true)) {
                                currentVideoId = parser.nextText().trim()
                            } else if (tagName.equals("title", ignoreCase = true)) {
                                currentTitle = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("entry", ignoreCase = true)) {
                            inEntry = false
                            if (currentVideoId.isNotBlank() && currentTitle.isNotBlank()) {
                                videos.add(
                                    ImportedVideoItem(
                                        youtubeId = currentVideoId,
                                        title = currentTitle,
                                        thumbnailUrl = "https://img.youtube.com/vi/$currentVideoId/hqdefault.jpg"
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing RSS XML", e)
        }

        return ImportedChannelResult(channelTitle, videos)
    }

    private fun extractVideosFromHtml(html: String): List<ImportedVideoItem> {
        val list = mutableListOf<ImportedVideoItem>()
        val seenIds = mutableSetOf<String>()

        val pattern = Pattern.compile("\"videoId\":\"([a-zA-Z0-9_-]{11})\"")
        val matcher = pattern.matcher(html)

        while (matcher.find()) {
            val videoId = matcher.group(1)
            if (videoId != null && seenIds.add(videoId)) {
                list.add(
                    ImportedVideoItem(
                        youtubeId = videoId,
                        title = "Vidéo $videoId",
                        thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                    )
                )
            }
        }
        return list
    }
}
