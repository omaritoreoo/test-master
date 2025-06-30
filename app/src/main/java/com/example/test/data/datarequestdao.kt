// File: app/src/main/test/data/ReqDatasetDao.kt
package com.example.test.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReqDatasetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReqDataset(req: DatasetRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReqDatasets(reqs: List<DatasetRequest>) // Metode baru untuk insert banyak data

    @Query("SELECT * FROM dataset_request ORDER BY id DESC")
    fun getAllReqDatasets(): Flow<List<DatasetRequest>>

    @Query("SELECT * FROM dataset_request WHERE id = :id")
    suspend fun getReqDatasetById(id: Int): DatasetRequest?

    @Update
    suspend fun updateReqDataset(req: DatasetRequest)

    @Delete
    suspend fun deleteReqDataset(req: DatasetRequest)

    @Query("DELETE FROM dataset_request") // Tambahkan metode untuk menghapus semua data
    suspend fun deleteAllReqDatasets()
}
