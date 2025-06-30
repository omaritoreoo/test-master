package com.example.test.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.lifecycle.LiveData

@Dao
interface MeaningfulObjectivesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meaningfulObjective: MeaningfulObjectives)

    @Query("SELECT * FROM meaningful_objectives WHERE projectId = :projectId LIMIT 1")
    fun getMeaningfulObjectivesForProject(projectId: Int): LiveData<MeaningfulObjectives?>

}