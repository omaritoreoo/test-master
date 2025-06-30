package com.example.test.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DataEntry::class, Profile::class,Project::class,DatasetRequest::class,
        DataProcessing::class, ModelTraining::class, User::class,MeaningfulObjectives::class],
    version = 62, // <-- NAIKKAN VERSI DATABASE! Penting untuk migrasi schema.
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dataEntryDao(): DataEntryDao
    abstract fun profileDao(): ProfileDao
    abstract fun projectDao(): ProjectDao
    abstract fun reqDatasetDao(): ReqDatasetDao
    abstract fun dataProcessingDao(): DataProcessingDao
    abstract fun modelTrainingDao(): ModelTrainingDao
    abstract fun userDao(): UserDao
    abstract fun meaningfulObjectivesDao(): MeaningfulObjectivesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}