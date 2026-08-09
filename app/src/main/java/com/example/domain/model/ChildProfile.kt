package com.example.domain.model

data class ChildProfile(
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String = "👦",
    val age: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)
