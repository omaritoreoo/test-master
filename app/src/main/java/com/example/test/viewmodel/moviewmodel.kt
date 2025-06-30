package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test.data.MeaningfulObjectives
import com.example.test.data.ProjectWithMeaningfulObjectives
import com.example.test.network.MeaningfulObjectiveRetrofitClient
import com.example.test.data.AppDatabase // Import AppDatabase
import com.example.test.repository.MeaningfulObjectiveRepository // Import repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeaningfulObjectivesViewModel(application: Application) : AndroidViewModel(application) {

    private val _apiMeaningfulObjectives = MutableLiveData<List<ProjectWithMeaningfulObjectives>>()
    val apiMeaningfulObjectives: LiveData<List<ProjectWithMeaningfulObjectives>> = _apiMeaningfulObjectives

    private val apiService = MeaningfulObjectiveRetrofitClient.instance

    private val meaningfulObjectivesDao = AppDatabase.getDatabase(application).meaningfulObjectivesDao()
    private val repository = MeaningfulObjectiveRepository(meaningfulObjectivesDao)

    init {
        fetchMeaningfulObjectivesFromApi()
    }

    private fun fetchMeaningfulObjectivesFromApi() {
        viewModelScope.launch {
            try {
                val response = apiService.getProjectsWithMeaningfulObjectives()
                if (response.isSuccessful) {
                    val projectsWithObjectives = response.body()
                    if (projectsWithObjectives != null) {
                        withContext(Dispatchers.Main) {
                            // TAMBAHKAN operator '!!' di sini
                            _apiMeaningfulObjectives.value = projectsWithObjectives!!
                        }
                        // Simpan meaningfulObjectives ke Room setelah diambil dari API
                        // Ini tetap di luar withContext(Dispatchers.Main) karena ini operasi I/O
                        projectsWithObjectives.forEach { projectWithMo ->
                            projectWithMo.meaningfulObjectives?.let { mo ->
                                mo.projectId = projectWithMo.id
                                repository.insertMeaningfulObjective(mo)
                            }
                        }
                    } else {
                        // Handle empty or null response body
                        withContext(Dispatchers.Main) {
                            _apiMeaningfulObjectives.value = emptyList()
                        }
                    }
                } else {
                    // Handle API error
                    // Log error: response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        _apiMeaningfulObjectives.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle network or parsing exceptions
                // Log exception: e.message
                withContext(Dispatchers.Main) {
                    _apiMeaningfulObjectives.value = emptyList()
                }
            }
        }
    }

    fun getMeaningfulObjectivesForProjectFromRoom(projectId: Int): LiveData<MeaningfulObjectives?> {
        return repository.getMeaningfulObjectivesForProject(projectId)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MeaningfulObjectivesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MeaningfulObjectivesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}