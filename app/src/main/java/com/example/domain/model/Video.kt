package com.example.domain.model

data class Video(
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
    val order: Int = 0,
    val description: String = "",
    val targetProfileId: Long = 0L
)
