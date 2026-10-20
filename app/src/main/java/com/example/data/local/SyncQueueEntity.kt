package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String, // "UPSERT_VIDEO", "DELETE_VIDEO", "UPSERT_PROFILE", "DELETE_PROFILE", "FULL_SYNC"
    val payloadJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING" // "PENDING", "PROCESSING", "FAILED"
)
