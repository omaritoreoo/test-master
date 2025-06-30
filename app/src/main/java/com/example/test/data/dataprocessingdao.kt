package com.example.test.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DataProcessingDao {
    @Query("SELECT * FROM data_processing WHERE projectName = :projectName ORDER BY createdAt DESC LIMIT 1")
    suspend fun getDataProcessingByProjectName(projectName: String): DataProcessing?

    @Query("SELECT * FROM data_processing ORDER BY createdAt DESC")
    fun getAllDataProcessing(): Flow<List<DataProcessing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: DataProcessing)

    @Update
    suspend fun update(data: DataProcessing)

    @Delete
    suspend fun delete(data: DataProcessing)

    @Query("SELECT * FROM data_processing WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DataProcessing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataList: List<DataProcessing>)

    @Query("DELETE FROM data_processing")
    suspend fun deleteAll()
}