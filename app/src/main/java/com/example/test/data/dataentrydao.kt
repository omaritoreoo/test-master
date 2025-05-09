package com.example.test.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DataEntryDao {
    @Query("SELECT * FROM dataentry ORDER BY id DESC")
    fun getAllEntries(): Flow<List<DataEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DataEntry)

    @Update
    suspend fun update(entry: DataEntry)

    @Delete
    suspend fun delete(entry: DataEntry)
}
