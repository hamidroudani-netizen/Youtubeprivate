package com.example.util

import android.content.Context
import android.content.Intent
import com.example.domain.model.Video

object ShareHelper {
    fun shareVideo(context: Context, video: Video) {
        val videoUrl = "https://www.youtube.com/watch?v=${video.youtubeId}"
        val message = "Regarde cette vidéo : ${video.title}\n$videoUrl"
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, video.title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Partager la vidéo via")
        context.startActivity(shareIntent)
    }

    fun shareVideoById(context: Context, youtubeId: String, title: String) {
        val videoUrl = "https://www.youtube.com/watch?v=$youtubeId"
        val message = "Regarde cette vidéo : $title\n$videoUrl"
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Partager la vidéo via")
        context.startActivity(shareIntent)
    }
}
