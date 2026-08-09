package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Video

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "Général",
    val favorite: Boolean = false,
    val duration: String = "",
    val lastPlayed: Long = 0L,
    val ageMin: Int = 3,
    val ageMax: Int = 10,
    val displayOrder: Int = 0,
    val description: String = "",
    val targetProfileId: Long = 0L
) {
    fun toDomain(): Video = Video(
        id = id,
        youtubeId = youtubeId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
        category = category,
        favorite = favorite,
        duration = duration,
        lastPlayed = lastPlayed,
        ageMin = ageMin,
        ageMax = ageMax,
        order = displayOrder,
        description = description,
        targetProfileId = targetProfileId
    )

    companion object {
        fun fromDomain(video: Video): VideoEntity = VideoEntity(
            id = video.id,
            youtubeId = video.youtubeId,
            title = video.title,
            thumbnailUrl = video.thumbnailUrl,
            createdAt = video.createdAt,
            category = video.category,
            favorite = video.favorite,
            duration = video.duration,
            lastPlayed = video.lastPlayed,
            ageMin = video.ageMin,
            ageMax = video.ageMax,
            displayOrder = video.order,
            description = video.description,
            targetProfileId = video.targetProfileId
        )
    }
}
