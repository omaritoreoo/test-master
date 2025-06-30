// File: app/src/main/test/viewmodel/DatasetRequestViewModel.kt
package com.example.test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.data.AppDatabase
import com.example.test.data.DatasetRequest
import com.example.test.data.DatasetRequestApiRequest
import com.example.test.network.DataRequestRetrofitClient
import com.example.test.network.RetrofitClient // Menggunakan RetrofitClient umum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DatasetRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val reqDatasetDao = AppDatabase.getDatabase(application).reqDatasetDao()
    private val datasetRequestApiService = DataRequestRetrofitClient.datasetRequestApiService

    // State untuk UI
    private val _allDatasetRequests = MutableStateFlow<List<DatasetRequest>>(emptyList())
    val allDatasetRequests: StateFlow<List<DatasetRequest>> = _allDatasetRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Mengamati perubahan dari Room DB
        viewModelScope.launch {
            reqDatasetDao.getAllReqDatasets().collectLatest { localData ->
                _allDatasetRequests.value = localData
            }
        }
        // Pemicu awal untuk mengambil data dari API
        fetchAndCacheAllDatasetRequests()
    }

    fun fetchAndCacheAllDatasetRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = datasetRequestApiService.getAllDatasetRequests()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponseList ->
                        val roomDataList = apiResponseList.map { apiResponse ->
                            DatasetRequest(
                                id = apiResponse.id,
                                projectName = apiResponse.projectDetail?.name,
                                projectId = apiResponse.projectDetail?.id,
                                projectDescription = apiResponse.projectDetail?.description, // Tambahkan ini
                                requestedByUsername = apiResponse.requestedByDetail?.username,
                                requestedById = apiResponse.requestedByDetail?.id,
                                descriptionProblem = apiResponse.descriptionProblem,
                                targetForDataset = apiResponse.targetForDataset,
                                typeDataNeeded = apiResponse.typeDataNeeded,
                                dataProcessingActivity = apiResponse.dataProcessingActivity,
                                numFeatures = apiResponse.numFeatures,
                                datasetSize = apiResponse.datasetSize,
                                fileFormat = apiResponse.fileFormat,
                                startDateNeeded = apiResponse.startDateNeeded,
                                endDateNeeded = apiResponse.endDateNeeded,
                                status = apiResponse.status,
                                createdAt = apiResponse.createdAt,
                                updatedAt = apiResponse.updatedAt
                            )
                        }
                        // Hapus semua data lama dan masukkan data baru dari API
                        reqDatasetDao.deleteAllReqDatasets() // Pastikan metode ini ada di DAO
                        reqDatasetDao.insertAllReqDatasets(roomDataList) // Pastikan metode ini ada di DAO
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Failed to fetch dataset requests: ${response.code()} - $errorBody"
                    Log.e("DatasetRequestVM", "Failed to fetch dataset requests: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
                Log.e("DatasetRequestVM", "Network error fetching dataset requests:", e)
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()} - ${e.message()}"
                Log.e("DatasetRequestVM", "HTTP error fetching dataset requests:", e)
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred: ${e.message}"
                Log.e("DatasetRequestVM", "An unexpected error occurred fetching dataset requests:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addDatasetRequest(request: DatasetRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val apiRequest = DatasetRequestApiRequest(
                    project = request.projectId,
                    requestedBy = request.requestedById,
                    descriptionProblem = request.descriptionProblem,
                    targetForDataset = request.targetForDataset,
                    typeDataNeeded = request.typeDataNeeded,
                    dataProcessingActivity = request.dataProcessingActivity,
                    numFeatures = request.numFeatures,
                    datasetSize = request.datasetSize,
                    fileFormat = request.fileFormat,
                    startDateNeeded = request.startDateNeeded,
                    endDateNeeded = request.endDateNeeded,
                    status = request.status
                )
                val response = datasetRequestApiService.createDatasetRequest(apiRequest)
                if (response.isSuccessful) {
                    response.body()?.let { createdResponse ->
                        val newLocalRequest = DatasetRequest(
                            id = createdResponse.id,
                            projectName = createdResponse.projectDetail?.name,
                            projectId = createdResponse.projectDetail?.id,
                            projectDescription = createdResponse.projectDetail?.description, // Tambahkan ini
                            requestedByUsername = createdResponse.requestedByDetail?.username,
                            requestedById = createdResponse.requestedByDetail?.id,
                            descriptionProblem = createdResponse.descriptionProblem,
                            targetForDataset = createdResponse.targetForDataset,
                            typeDataNeeded = createdResponse.typeDataNeeded,
                            dataProcessingActivity = createdResponse.dataProcessingActivity,
                            numFeatures = createdResponse.numFeatures,
                            datasetSize = createdResponse.datasetSize,
                            fileFormat = createdResponse.fileFormat,
                            startDateNeeded = createdResponse.startDateNeeded,
                            endDateNeeded = createdResponse.endDateNeeded,
                            status = createdResponse.status,
                            createdAt = createdResponse.createdAt,
                            updatedAt = createdResponse.updatedAt
                        )
                        reqDatasetDao.insertReqDataset(newLocalRequest)
                        fetchAndCacheAllDatasetRequests() // Refresh data setelah penambahan
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Failed to add dataset request: ${response.code()} - $errorBody"
                    Log.e("DatasetRequestVM", "Failed to add dataset request: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                _error.value = "Network error adding dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "Network error adding dataset request:", e)
            } catch (e: HttpException) {
                _error.value = "HTTP error adding dataset request: ${e.code()} - ${e.message()}"
                Log.e("DatasetRequestVM", "HTTP error adding dataset request:", e)
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred adding dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "An unexpected error occurred adding dataset request:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDatasetRequest(request: DatasetRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val apiRequest = DatasetRequestApiRequest(
                    project = request.projectId,
                    requestedBy = request.requestedById,
                    descriptionProblem = request.descriptionProblem,
                    targetForDataset = request.targetForDataset,
                    typeDataNeeded = request.typeDataNeeded,
                    dataProcessingActivity = request.dataProcessingActivity,
                    numFeatures = request.numFeatures,
                    datasetSize = request.datasetSize,
                    fileFormat = request.fileFormat,
                    startDateNeeded = request.startDateNeeded,
                    endDateNeeded = request.endDateNeeded,
                    status = request.status
                )
                val response = datasetRequestApiService.updateDatasetRequest(request.id, apiRequest)
                if (response.isSuccessful) {
                    response.body()?.let { updatedResponse ->
                        val updatedLocalRequest = DatasetRequest(
                            id = updatedResponse.id,
                            projectName = updatedResponse.projectDetail?.name,
                            projectId = updatedResponse.projectDetail?.id,
                            projectDescription = updatedResponse.projectDetail?.description, // Tambahkan ini
                            requestedByUsername = updatedResponse.requestedByDetail?.username,
                            requestedById = updatedResponse.requestedByDetail?.id,
                            descriptionProblem = updatedResponse.descriptionProblem,
                            targetForDataset = updatedResponse.targetForDataset,
                            typeDataNeeded = updatedResponse.typeDataNeeded,
                            dataProcessingActivity = updatedResponse.dataProcessingActivity,
                            numFeatures = updatedResponse.numFeatures,
                            datasetSize = updatedResponse.datasetSize,
                            fileFormat = updatedResponse.fileFormat,
                            startDateNeeded = updatedResponse.startDateNeeded,
                            endDateNeeded = updatedResponse.endDateNeeded,
                            status = updatedResponse.status,
                            createdAt = updatedResponse.createdAt,
                            updatedAt = updatedResponse.updatedAt
                        )
                        reqDatasetDao.updateReqDataset(updatedLocalRequest)
                        fetchAndCacheAllDatasetRequests() // Refresh data setelah pembaruan
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Failed to update dataset request: ${response.code()} - $errorBody"
                    Log.e("DatasetRequestVM", "Failed to update dataset request: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                _error.value = "Network error updating dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "Network error updating dataset request:", e)
            } catch (e: HttpException) {
                _error.value = "HTTP error updating dataset request: ${e.code()} - ${e.message()}"
                Log.e("DatasetRequestVM", "HTTP error updating dataset request:", e)
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred updating dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "An unexpected error occurred updating dataset request:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDatasetRequest(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = datasetRequestApiService.deleteDatasetRequest(id)
                if (response.isSuccessful) {
                    val datasetToDelete = reqDatasetDao.getReqDatasetById(id)
                    if (datasetToDelete != null) {
                        reqDatasetDao.deleteReqDataset(datasetToDelete)
                    }
                    fetchAndCacheAllDatasetRequests() // Refresh data setelah penghapusan
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Failed to delete dataset request: ${response.code()} - $errorBody"
                    Log.e("DatasetRequestVM", "Failed to delete dataset request: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                _error.value = "Network error deleting dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "Network error deleting dataset request:", e)
            } catch (e: HttpException) {
                _error.value = "HTTP error deleting dataset request: ${e.code()} - ${e.message()}"
                Log.e("DatasetRequestVM", "HTTP error deleting dataset request:", e)
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred deleting dataset request: ${e.message}"
                Log.e("DatasetRequestVM", "An unexpected error occurred deleting dataset request:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DatasetRequestViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return DatasetRequestViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}