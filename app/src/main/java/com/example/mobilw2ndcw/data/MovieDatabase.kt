package com.example.mobilw2ndcw.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Room database for movies (version 2) */
@Database(entities = [Movie::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    /** Access to Movie DAO */
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        /** Get database instance (creates if needed) */
        suspend fun getDatabase(context: Context): MovieDatabase = withContext(Dispatchers.IO) {
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                )
                .fallbackToDestructiveMigration() // Recreates DB on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 