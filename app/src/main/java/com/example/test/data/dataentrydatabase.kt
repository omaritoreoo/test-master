package com.example.test.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataEntry::class], version = 1, exportSchema = false)
abstract class DataEntryDatabase : RoomDatabase() {
    abstract fun dataEntryDao(): DataEntryDao

    companion object {
        @Volatile
        private var INSTANCE: DataEntryDatabase? = null

        fun getDatabase(context: Context): DataEntryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataEntryDatabase::class.java,
                    "dataentry_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
