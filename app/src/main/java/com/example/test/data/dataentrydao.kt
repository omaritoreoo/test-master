// File: app/src/main/test/data/DataEntryDao.kt
package com.example.test.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface DataEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: DataEntry): Long

    @Update
    suspend fun update(data: DataEntry)

    @Delete
    suspend fun delete(data: DataEntry)

    @Query("SELECT * FROM data_entry ORDER BY id DESC")
    fun getAll(): Flow<List<DataEntry>>

    @Query("SELECT * FROM data_entry WHERE projectName = :projectName ORDER BY dateCreated DESC LIMIT 1")
    suspend fun getProblemFramingByProjectName(projectName: String): DataEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataEntries: List<DataEntry>)

    @Query("DELETE FROM data_entry")
    suspend fun deleteAll()
}