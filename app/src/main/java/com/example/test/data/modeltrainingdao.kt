// File: app/src/main/test/data/ModelTrainingDao.kt
package com.example.test.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelTrainingDao {
    @Query("SELECT * FROM model_training ORDER BY id DESC")
    fun getAll(): Flow<List<ModelTraining>> // Mengembalikan Flow untuk semua

    // Fungsi yang Anda minta: mendapatkan ModelTraining berdasarkan projectName
    @Query("SELECT * FROM model_training WHERE projectName = :projectName ORDER BY id DESC LIMIT 1")
    suspend fun getModelTrainingByProjectName(projectName: String): ModelTraining?

    // Fungsi standar lainnya yang penting untuk CRUD dan sinkronisasi
    @Query("SELECT * FROM model_training WHERE id = :id")
    fun getById(id: Int): Flow<ModelTraining> // Untuk mendapatkan satu entri berdasarkan ID dari database lokal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(training: ModelTraining)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trainings: List<ModelTraining>) // Untuk menyisipkan banyak entri, berguna saat sinkronisasi dari API

    @Update
    suspend fun update(training: ModelTraining)

    @Delete
    suspend fun delete(training: ModelTraining)

    @Query("DELETE FROM model_training")
    suspend fun deleteAll() // Untuk menghapus semua data (berguna sebelum menyisipkan data baru dari API)
}