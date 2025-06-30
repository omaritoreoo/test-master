// File: app/src/main/test/viewmodel/DataEntryViewModel.kt
package com.example.test.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.data.AppDatabase
import com.example.test.data.DataEntry
import com.example.test.data.DataEntryRequest
import com.example.test.data.DataEntryResponse
import com.example.test.data.Project
import com.example.test.data.ProjectRepository
import com.example.test.network.DataEntryRetrofitClient // Menggunakan DataEntryRetrofitClient
import com.example.test.network.RetrofitClient // Import RetrofitClient (untuk generalApiService)
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DataEntryViewModel(application: Application) : AndroidViewModel(application) {
    // Inisialisasi DAO dan API Service yang relevan untuk DataEntry
    private val dataEntryDao = AppDatabase.getDatabase(application).dataEntryDao()
    private val dataEntryApiService = DataEntryRetrofitClient.dataEntryApiService // Menggunakan DataEntryRetrofitClient

    // Inisialisasi DAO lainnya yang diperlukan oleh ProjectRepository
    private val projectDao = AppDatabase.getDatabase(application).projectDao()
    private val dataProcessingDao = AppDatabase.getDatabase(application).dataProcessingDao()
    private val modelTrainingDao = AppDatabase.getDatabase(application).modelTrainingDao()

    // Ambil apiService yang dibutuhkan oleh ProjectRepository Anda
    // Asumsi: RetrofitClient.apiService ada dan berfungsi.
    private val generalApiService = RetrofitClient.apiService // Menggunakan apiService dari RetrofitClient

    // Inisialisasi ProjectRepository dengan parameter yang sesuai dengan definisi Anda
    private val projectRepository = ProjectRepository(
        projectDao,
        generalApiService, // Meneruskan apiService yang dibutuhkan ProjectRepository
        dataEntryDao,
        dataProcessingDao,
        modelTrainingDao
    )

    // Mengambil semua DataEntry (sekarang langsung dari Room DAO di ViewModel ini)
    val allEntries: LiveData<List<DataEntry>> = dataEntryDao.getAll().asLiveData()

    // Mengambil semua Projects melalui ProjectRepository (sesuai dengan definisi Anda)
    val allProjects: LiveData<List<Project>> = projectRepository.getCombinedProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()

    var selectedEntry = mutableStateOf<DataEntry?>(null)

    init {
        refreshDataFromApi()
    }

    fun refreshDataFromApi() = viewModelScope.launch {
        try {
            val response = dataEntryApiService.getAllDataEntries()
            if (response.isSuccessful) {
                response.body()?.let { apiDataList ->
                    val roomDataList = apiDataList.map { apiResponse ->
                        DataEntry(
                            id = apiResponse.id,
                            projectName = apiResponse.projectDetail?.name,
                            projectId = apiResponse.projectDetail?.id,
                            problemDescription = apiResponse.problemDescription,
                            target = apiResponse.targetGoal,
                            stock = apiResponse.stockInitialState,
                            inflow = apiResponse.inputInflowDescription,
                            outflow = apiResponse.outputOutflowDescription,
                            dataNeeded = apiResponse.keyFeaturesData,
                            framedBy = apiResponse.framedByDetail?.username,
                            framedById = apiResponse.framedBy,
                            dateCreated = apiResponse.createdAt,
                            createdAt = apiResponse.createdAt,
                            updatedAt = apiResponse.updatedAt
                        )
                    }
                    dataEntryDao.deleteAll()
                    dataEntryDao.insertAll(roomDataList)
                }
            } else {
                println("Error fetching data entries from API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data entry fetching. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error fetching data entries: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error fetching data entries: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 401) {
                println("Authentication failed for data entry fetching. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while fetching data entries: ${e.message}")
        }
    }

    fun insertEntry(entry: DataEntry) = viewModelScope.launch {
        try {
            val request = DataEntryRequest(
                project = entry.projectId,
                framedBy = entry.framedById,
                problemDescription = entry.problemDescription.orEmpty(), // Pastikan tidak null
                targetGoal = entry.target.orEmpty(), // Pastikan tidak null
                stockInitialState = entry.stock.orEmpty(), // Pastikan tidak null
                inputInflowDescription = entry.inflow.orEmpty(), // Pastikan tidak null
                outputOutflowDescription = entry.outflow.orEmpty(), // Pastikan tidak null
                keyFeaturesData = entry.dataNeeded.orEmpty() // Pastikan tidak null
            )
            val response = dataEntryApiService.createDataEntry(request)
            if (response.isSuccessful) {
                response.body()?.let { createdResponse ->
                    val createdData = DataEntry(
                        id = createdResponse.id,
                        projectName = createdResponse.projectDetail?.name,
                        projectId = createdResponse.projectDetail?.id,
                        problemDescription = createdResponse.problemDescription,
                        target = createdResponse.targetGoal,
                        stock = createdResponse.stockInitialState,
                        inflow = createdResponse.inputInflowDescription,
                        outflow = createdResponse.outputOutflowDescription,
                        dataNeeded = createdResponse.keyFeaturesData,
                        framedBy = createdResponse.framedByDetail?.username,
                        framedById = createdResponse.framedBy,
                        dateCreated = createdResponse.createdAt,
                        createdAt = createdResponse.createdAt,
                        updatedAt = createdResponse.updatedAt
                    )
                    dataEntryDao.insert(createdData)
                    refreshDataFromApi()
                }
            } else {
                println("Error creating data entry via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data entry creation. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error creating data entry: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error creating data entry: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 401) {
                println("Authentication failed for data entry creation. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while creating data entry: ${e.message}")
        }
    }

    fun updateEntry(entry: DataEntry) = viewModelScope.launch {
        try {
            val request = DataEntryRequest(
                project = entry.projectId,
                framedBy = entry.framedById,
                problemDescription = entry.problemDescription.orEmpty(), // Pastikan tidak null
                targetGoal = entry.target.orEmpty(), // Pastikan tidak null
                stockInitialState = entry.stock.orEmpty(), // Pastikan tidak null
                inputInflowDescription = entry.inflow.orEmpty(), // Pastikan tidak null
                outputOutflowDescription = entry.outflow.orEmpty(), // Pastikan tidak null
                keyFeaturesData = entry.dataNeeded.orEmpty() // Pastikan tidak null
            )
            val response = dataEntryApiService.updateDataEntry(entry.id, request)
            if (response.isSuccessful) {
                response.body()?.let { updatedResponse ->
                    val updatedData = DataEntry(
                        id = updatedResponse.id,
                        projectName = updatedResponse.projectDetail?.name,
                        projectId = updatedResponse.projectDetail?.id,
                        problemDescription = updatedResponse.problemDescription,
                        target = updatedResponse.targetGoal,
                        stock = updatedResponse.stockInitialState,
                        inflow = updatedResponse.inputInflowDescription,
                        outflow = updatedResponse.outputOutflowDescription,
                        dataNeeded = updatedResponse.keyFeaturesData,
                        framedBy = updatedResponse.framedByDetail?.username,
                        framedById = updatedResponse.framedBy,
                        dateCreated = updatedResponse.createdAt,
                        createdAt = updatedResponse.createdAt,
                        updatedAt = updatedResponse.updatedAt
                    )
                    dataEntryDao.update(updatedData)
                    refreshDataFromApi()
                }
            } else {
                println("Error updating data entry via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data entry update. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error updating data entry: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error updating data entry: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 401) {
                println("Authentication failed for data entry update. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while updating data entry: ${e.message}")
        }
    }

    fun deleteEntry(entry: DataEntry) = viewModelScope.launch {
        try {
            val response = dataEntryApiService.deleteDataEntry(entry.id)
            if (response.isSuccessful) {
                dataEntryDao.delete(entry)
                refreshDataFromApi()
            } else {
                println("Error deleting data entry via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data entry deletion. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error deleting data entry: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error deleting data entry: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                println("Authentication failed for data entry deletion. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while deleting data entry: ${e.message}")
        }
    }

    fun setSelectedEntry(entry: DataEntry) {
        selectedEntry.value = entry
    }

    // Fungsi ini akan memanggil ProjectRepository yang hanya melakukan operasi Room
    suspend fun insertEntryAndGetId(entry: DataEntry): Long {
        println("Note: insertEntryAndGetId calls ProjectRepository, which only performs Room insert for DataEntry. It does NOT trigger an API call.")
        return projectRepository.insertDataEntry(entry)
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DataEntryViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return DataEntryViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
