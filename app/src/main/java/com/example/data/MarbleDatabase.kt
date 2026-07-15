package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MarbleProduct::class], version = 1, exportSchema = false)
abstract class MarbleDatabase : RoomDatabase() {
    abstract fun marbleDao(): MarbleDao

    companion object {
        @Volatile
        private var INSTANCE: MarbleDatabase? = null

        fun getDatabase(context: Context): MarbleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarbleDatabase::class.java,
                    "moonstones_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
