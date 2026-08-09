package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    suspend fun getAllProfilesList(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM child_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("DELETE FROM child_profiles WHERE id NOT IN (:ids)")
    suspend fun deleteProfilesNotIn(ids: List<Long>)
}
