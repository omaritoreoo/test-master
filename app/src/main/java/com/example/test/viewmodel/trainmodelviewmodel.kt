// File: app/src/main/test/viewmodel/ModelTrainingViewModel.kt
package com.example.test.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.data.AppDatabase
import com.example.test.data.ModelTraining
import com.example.test.data.ModelTrainingResponse
import com.example.test.network.ModelTrainingRetrofitClient
import com.example.test.network.RetrofitClient // KOREKSI: Menggunakan RetrofitClient yang benar
import com.google.gson.Gson // Untuk mengonversi Map ke JSON string
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModelTrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).modelTrainingDao()
    private val gson = Gson() // Inisialisasi Gson
    val allModelTrainings: LiveData<List<ModelTraining>> = dao.getAll().asLiveData()
    init {
        refreshDataFromApi()
    }
    private fun String?.toRequestBodyOrEmpty(): RequestBody {
        return (this ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
    }
    private fun Int?.toRequestBodyOrEmpty(): RequestBody {
        return (this?.toString() ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
    }
    private fun Double?.toRequestBodyOrEmpty(): RequestBody {
        return (this?.toString() ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
    }
    private fun prepareFilePart(context: Context, fileUri: Uri?): MultipartBody.Part? {
        fileUri ?: return null
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, fileUri) ?: "uploaded_file"
        val mediaType = contentResolver.getType(fileUri)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
        val file = File(context.cacheDir, fileName)
        try {
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            println("Error creating temporary file for upload: ${e.message}")
            return null
        }

        val requestBody = file.asRequestBody(mediaType)
        // Nama field harus 'model_path' sesuai dengan backend Django
        return MultipartBody.Part.createFormData("model_path", fileName, requestBody)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return null
    }

    // --- End Helper Functions ---


    fun refreshDataFromApi() = viewModelScope.launch {
        try {
            val response = ModelTrainingRetrofitClient.modelTrainingApiService.getAllModelTrainings() // KOREKSI PENGGUNAAN
            if (response.isSuccessful) {
                response.body()?.let { apiDataList ->
                    val roomDataList = apiDataList.map { apiResponse ->
                        // Pemetaan dari ModelTrainingResponse (API) ke ModelTraining (Room)
                        ModelTraining(
                            id = apiResponse.id,
                            projectName = apiResponse.projectDetail?.name,
                            projectId = apiResponse.projectDetail?.id, // Mengambil projectId dari API Response
                            modelName = apiResponse.modelName,
                            modelType = apiResponse.modelType,
                            algorithm = apiResponse.algorithmUsed,
                            trainingData = apiResponse.trainingDataUsedDetail?.dataSourceDescription, // Mapping ke string
                            performance = apiResponse.modelPerformance?.accuracy?.toString(), // Mapping ke string (ambil akurasi)
                            modelPath = apiResponse.modelPath,
                            refinementStrategy = apiResponse.refiningStrategy,
                            performanceAfterRefinement = apiResponse.refiningStatus, // Mapping ke string
                            trainingDataUsedId = apiResponse.trainingDataUsed,
                            trainedById = apiResponse.trainedBy,
                            trainedByUsername = apiResponse.trainedByDetail?.username,
                            trainingDate = apiResponse.trainingDate,
                            createdAt = apiResponse.createdAt,
                            updatedAt = apiResponse.updatedAt
                        )
                    }
                    dao.deleteAll() // Hapus data lama
                    dao.insertAll(roomDataList) // Masukkan data baru
                }
            } else {
                println("Error fetching model training data from API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for model training data fetching. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error fetching model training data: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error fetching model training data: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                println("Authentication failed for model training data fetching. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while fetching model training data: ${e.message}")
        }
    }

    fun createModelTraining(data: ModelTraining, fileUri: Uri?) = viewModelScope.launch {
        try {
            // Konversi semua field ke RequestBody
            val projectRequestBody = data.projectId.toRequestBodyOrEmpty() // KOREKSI: Menggunakan data.projectId
            val trainingDataUsedRequestBody = data.trainingDataUsedId.toRequestBodyOrEmpty()
            val trainedByRequestBody = data.trainedById.toRequestBodyOrEmpty()
            val modelNameRequestBody = data.modelName.toRequestBodyOrEmpty()
            val modelTypeRequestBody = data.modelType.toRequestBodyOrEmpty()
            val algorithmRequestBody = data.algorithm.toRequestBodyOrEmpty()

            // Konversi model_performance ke JSON string
            val performanceMap = mutableMapOf<String, Any?>()
            data.performance?.toDoubleOrNull()?.let { performanceMap["accuracy"] = it } // Ambil dari field 'performance'
            val modelPerformanceJson = gson.toJson(performanceMap)
            val modelPerformanceRequestBody = modelPerformanceJson.toRequestBodyOrEmpty()

            val refinementStrategyRequestBody = data.refinementStrategy.toRequestBodyOrEmpty()
            val refiningStatusRequestBody = data.performanceAfterRefinement.toRequestBodyOrEmpty() // Mapping dari performanceAfterRefinement
            val trainingDateRequestBody = data.trainingDate.toRequestBodyOrEmpty()

            // Siapkan bagian file
            val modelPathPart = prepareFilePart(getApplication(), fileUri)

            val response = ModelTrainingRetrofitClient.modelTrainingApiService.createModelTraining( // KOREKSI PENGGUNAAN
                project = projectRequestBody,
                trainingDataUsed = trainingDataUsedRequestBody,
                trainedBy = trainedByRequestBody,
                modelName = modelNameRequestBody,
                modelType = modelTypeRequestBody,
                algorithmUsed = algorithmRequestBody,
                modelPerformance = modelPerformanceRequestBody,
                modelPath = modelPathPart,
                refiningStrategy = refinementStrategyRequestBody,
                refiningStatus = refiningStatusRequestBody,
                trainingDate = trainingDateRequestBody
            )
            if (response.isSuccessful) {
                response.body()?.let { createdResponse ->
                    // Pemetaan kembali dari API Response ke Room Entity setelah sukses POST
                    val createdData = ModelTraining(
                        id = createdResponse.id,
                        projectName = createdResponse.projectDetail?.name,
                        projectId = createdResponse.projectDetail?.id, // Mengambil projectId dari API Response
                        modelName = createdResponse.modelName,
                        modelType = createdResponse.modelType,
                        algorithm = createdResponse.algorithmUsed,
                        trainingData = createdResponse.trainingDataUsedDetail?.dataSourceDescription,
                        performance = createdResponse.modelPerformance?.accuracy?.toString(),
                        modelPath = createdResponse.modelPath,
                        refinementStrategy = createdResponse.refiningStrategy,
                        performanceAfterRefinement = createdResponse.refiningStatus,
                        trainingDataUsedId = createdResponse.trainingDataUsed,
                        trainedById = createdResponse.trainedBy,
                        trainedByUsername = createdResponse.trainedByDetail?.username,
                        trainingDate = createdResponse.trainingDate,
                        createdAt = createdResponse.createdAt,
                        updatedAt = createdResponse.updatedAt
                    )
                    dao.insert(createdData) // Simpan ke Room
                    refreshDataFromApi() // Perbarui daftar dari API
                }
            } else {
                println("Error creating model training via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for model training creation. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error creating model training: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error creating model training: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 401) {
                println("Authentication failed for model training creation. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while creating model training: ${e.message}")
        }
    }

    fun updateModelTraining(id: Int, data: ModelTraining, fileUri: Uri?) = viewModelScope.launch {
        try {
            // Konversi semua field ke RequestBody
            val projectRequestBody = data.projectId.toRequestBodyOrEmpty() // KOREKSI: Menggunakan data.projectId
            val trainingDataUsedRequestBody = data.trainingDataUsedId.toRequestBodyOrEmpty()
            val trainedByRequestBody = data.trainedById.toRequestBodyOrEmpty()
            val modelNameRequestBody = data.modelName.toRequestBodyOrEmpty()
            val modelTypeRequestBody = data.modelType.toRequestBodyOrEmpty()
            val algorithmRequestBody = data.algorithm.toRequestBodyOrEmpty()

            // Konversi model_performance ke JSON string
            val performanceMap = mutableMapOf<String, Any?>()
            data.performance?.toDoubleOrNull()?.let { performanceMap["accuracy"] = it } // Ambil dari field 'performance'
            val modelPerformanceJson = gson.toJson(performanceMap)
            val modelPerformanceRequestBody = modelPerformanceJson.toRequestBodyOrEmpty()

            val refinementStrategyRequestBody = data.refinementStrategy.toRequestBodyOrEmpty()
            val refiningStatusRequestBody = data.performanceAfterRefinement.toRequestBodyOrEmpty() // Mapping dari performanceAfterRefinement
            val trainingDateRequestBody = data.trainingDate.toRequestBodyOrEmpty()

            // Siapkan bagian file.
            val modelPathPart = prepareFilePart(getApplication(), fileUri)

            val response = ModelTrainingRetrofitClient.modelTrainingApiService.updateModelTraining( // KOREKSI PENGGUNAAN
                id = id,
                project = projectRequestBody,
                trainingDataUsed = trainingDataUsedRequestBody,
                trainedBy = trainedByRequestBody,
                modelName = modelNameRequestBody,
                modelType = modelTypeRequestBody,
                algorithmUsed = algorithmRequestBody,
                modelPerformance = modelPerformanceRequestBody,
                modelPath = modelPathPart,
                refiningStrategy = refinementStrategyRequestBody,
                refiningStatus = refiningStatusRequestBody,
                trainingDate = trainingDateRequestBody
            )
            if (response.isSuccessful) {
                response.body()?.let { updatedResponse ->
                    // Pemetaan kembali dari API Response ke Room Entity setelah sukses PUT
                    val updatedData = ModelTraining(
                        id = updatedResponse.id,
                        projectName = updatedResponse.projectDetail?.name,
                        projectId = updatedResponse.projectDetail?.id, // Mengambil projectId dari API Response
                        modelName = updatedResponse.modelName,
                        modelType = updatedResponse.modelType,
                        algorithm = updatedResponse.algorithmUsed,
                        trainingData = updatedResponse.trainingDataUsedDetail?.dataSourceDescription,
                        performance = updatedResponse.modelPerformance?.accuracy?.toString(),
                        modelPath = updatedResponse.modelPath,
                        refinementStrategy = updatedResponse.refiningStrategy,
                        performanceAfterRefinement = updatedResponse.refiningStatus,
                        trainingDataUsedId = updatedResponse.trainingDataUsed,
                        trainedById = updatedResponse.trainedBy,
                        trainedByUsername = updatedResponse.trainedByDetail?.username,
                        trainingDate = updatedResponse.trainingDate,
                        createdAt = updatedResponse.createdAt,
                        updatedAt = updatedResponse.updatedAt
                    )
                    dao.update(updatedData) // Perbarui di Room
                    refreshDataFromApi() // Perbarui daftar dari API
                }
            } else {
                println("Error updating model training via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for model training update. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error updating model training: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error updating model training: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 401) {
                println("Authentication failed for model training update. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while updating data: ${e.message}")
        }
    }

    fun deleteModelTraining(data: ModelTraining) = viewModelScope.launch {
        try {
            val response = ModelTrainingRetrofitClient.modelTrainingApiService.deleteModelTraining(data.id) // KOREKSI PENGGUNAAN
            if (response.isSuccessful) {
                dao.delete(data) // Hapus dari Room
                refreshDataFromApi() // Perbarui daftar dari API
            } else {
                println("Error deleting model training via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for model training deletion. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error deleting model training: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error deleting model training: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                println("Authentication failed for model training deletion. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while deleting data: ${e.message}")
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ModelTrainingViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ModelTrainingViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
