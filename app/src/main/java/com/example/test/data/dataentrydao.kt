package com.example.test.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DataEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DataEntry)

    @Query("SELECT * FROM dataentry ORDER BY id DESC")
    fun getAllEntries(): Flow<List<DataEntry>>
}
