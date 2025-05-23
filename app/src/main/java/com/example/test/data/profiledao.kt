package com.example.test.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile_table WHERE id = 1 LIMIT 1")
    fun getProfile(): LiveData<Profile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile)

    @Query("DELETE FROM profile_table")
    suspend fun deleteAll()
}
