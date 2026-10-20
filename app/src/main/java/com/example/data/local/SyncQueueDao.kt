package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTask(task: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingTasks(): List<SyncQueueEntity>

    @Update
    suspend fun updateSyncTask(task: SyncQueueEntity)

    @Delete
    suspend fun deleteSyncTask(task: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncTaskById(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
