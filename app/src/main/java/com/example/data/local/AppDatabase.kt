package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [VideoEntity::class, ProfileEntity::class, SyncQueueEntity::class, WatchHistoryEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun profileDao(): ProfileDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "youtube_prive_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.videoDao().insertVideos(InitialDataSeed.getInitialVideos())
                                database.profileDao().insertProfile(ProfileEntity(name = "Léo", avatarEmoji = "👦", age = 5))
                                database.profileDao().insertProfile(ProfileEntity(name = "Sofia", avatarEmoji = "👧", age = 8))
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                InitialDataSeed.FAKE_YOUTUBE_IDS.forEach { fakeId ->
                                    database.videoDao().deleteByYoutubeId(fakeId)
                                }
                                if (database.videoDao().getVideoByYoutubeId("9iW5fBH6PRQ") == null) {
                                    database.videoDao().insertVideos(InitialDataSeed.getInitialVideos())
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
