package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "watch_history",
    primaryKeys = ["profileId", "youtubeId"]
)
data class WatchHistoryEntity(
    val profileId: String,
    val youtubeId: String,
    val positionSeconds: Long = 0L,
    val durationSeconds: Long = 0L,
    val progressPercentage: Int = 0,
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false
)
