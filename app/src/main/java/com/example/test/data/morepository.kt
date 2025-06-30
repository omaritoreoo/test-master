package com.example.test.repository

import com.example.test.data.MeaningfulObjectives
import com.example.test.data.MeaningfulObjectivesDao
import androidx.lifecycle.LiveData

class MeaningfulObjectiveRepository(private val meaningfulObjectivesDao: MeaningfulObjectivesDao) {

    suspend fun insertMeaningfulObjective(meaningfulObjective: MeaningfulObjectives) {
        meaningfulObjectivesDao.insert(meaningfulObjective)
    }

    fun getMeaningfulObjectivesForProject(projectId: Int): LiveData<MeaningfulObjectives?> {
        return meaningfulObjectivesDao.getMeaningfulObjectivesForProject(projectId)
    }
}