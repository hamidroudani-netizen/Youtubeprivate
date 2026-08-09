package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [VideoEntity::class, ProfileEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun profileDao(): ProfileDao

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
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
