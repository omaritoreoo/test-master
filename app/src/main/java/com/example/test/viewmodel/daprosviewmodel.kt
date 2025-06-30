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
import com.example.test.data.DataProcessing
import com.example.test.data.DataProcessingRequest // Masih digunakan untuk GET response mapping
import com.example.test.data.DataProcessingResponse
import com.example.test.network.DataProcessingRetrofitClient
import com.example.test.network.RetrofitClient
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

class DataProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dataProcessingDao()

    val allDataProcessing: LiveData<List<DataProcessing>> = dao.getAllDataProcessing().asLiveData()

    init {
        refreshDataFromApi()
    }

    // --- Helper Functions for Multipart Data ---

    // Helper untuk mengubah String menjadi RequestBody
    private fun String?.toRequestBodyOrEmpty(): RequestBody {
        return (this ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // Helper untuk mengubah Int menjadi RequestBody
    private fun Int?.toRequestBodyOrEmpty(): RequestBody {
        return (this?.toString() ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // Helper untuk membuat MultipartBody.Part dari Uri
    // Ini adalah bagian krusial untuk mengunggah file
    private fun prepareFilePart(context: Context, fileUri: Uri?): MultipartBody.Part? {
        fileUri ?: return null

        val contentResolver = context.contentResolver
        val fileName = getFileName(context, fileUri) ?: "uploaded_file"
        val mediaType = contentResolver.getType(fileUri)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()

        // Membuat file sementara dari URI untuk diunggah
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
        return MultipartBody.Part.createFormData("processed_file", fileName, requestBody)
    }

    // Fungsi helper untuk mendapatkan nama file dari Uri (sama seperti di DataProcessingScreen)
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
            val response = DataProcessingRetrofitClient.dataProcessingApiService.getAllDataProcessing()
            if (response.isSuccessful) {
                response.body()?.let { apiDataList ->
                    val roomDataList = apiDataList.map { apiResponse ->
                        DataProcessing(
                            id = apiResponse.id,
                            projectName = apiResponse.projectDetail?.name ?: "Unknown Project",
                            projectId = apiResponse.projectDetail?.id ?: 0,
                            dataSourceDescription = apiResponse.dataSourceDescription ?: "",
                            processingStepsSummary = apiResponse.processingStepsSummary ?: "",
                            featureEngineeringDetails = apiResponse.featureEngineeringDetails ?: "",
                            processedDataLocation = apiResponse.processedDataLocation ?: "",
                            processedFile = apiResponse.processedFile ?: "",
                            processingStatus = apiResponse.processingStatus ?: "",
                            createdAt = apiResponse.createdAt ?: "",
                            updatedAt = apiResponse.updatedAt ?: "",
                            processedBy = apiResponse.processedBy
                        )
                    }
                    dao.deleteAll()
                    dao.insertAll(roomDataList)
                }
            } else {
                println("Error fetching data from API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data fetching. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error fetching data: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error fetching data: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                println("Authentication failed for data fetching. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while fetching data: ${e.message}")
        }
    }

    // MODIFIKASI: Menerima Uri untuk file
    fun createDataProcessing(data: DataProcessing, fileUri: Uri?) = viewModelScope.launch {
        try {
            // Konversi semua field ke RequestBody
            val projectRequestBody = data.projectId.toRequestBodyOrEmpty()
            val dataSourceDescRequestBody = data.dataSourceDescription.toRequestBodyOrEmpty()
            val processingStepsSummaryRequestBody = data.processingStepsSummary.toRequestBodyOrEmpty()
            val featureEngineeringDetailsRequestBody = data.featureEngineeringDetails.toRequestBodyOrEmpty()
            val processedDataLocationRequestBody = data.processedDataLocation.toRequestBodyOrEmpty()
            val processingStatusRequestBody = data.processingStatus.toRequestBodyOrEmpty()
            val processedByRequestBody = data.processedBy.toRequestBodyOrEmpty()

            // Siapkan bagian file
            val processedFilePart = prepareFilePart(getApplication(), fileUri)

            val response = DataProcessingRetrofitClient.dataProcessingApiService.createDataProcessing(
                project = projectRequestBody,
                dataSourceDescription = dataSourceDescRequestBody,
                processingStepsSummary = processingStepsSummaryRequestBody,
                featureEngineeringDetails = featureEngineeringDetailsRequestBody,
                processedDataLocation = processedDataLocationRequestBody,
                processedFile = processedFilePart,
                processingStatus = processingStatusRequestBody,
                processedBy = processedByRequestBody
            )
            if (response.isSuccessful) {
                response.body()?.let { createdResponse ->
                    val createdData = DataProcessing(
                        id = createdResponse.id,
                        projectName = createdResponse.projectDetail?.name ?: "Unknown Project",
                        projectId = createdResponse.projectDetail?.id ?: 0,
                        dataSourceDescription = createdResponse.dataSourceDescription ?: "",
                        processingStepsSummary = createdResponse.processingStepsSummary ?: "",
                        featureEngineeringDetails = createdResponse.featureEngineeringDetails ?: "",
                        processedDataLocation = createdResponse.processedDataLocation ?: "",
                        processedFile = createdResponse.processedFile ?: "",
                        processingStatus = createdResponse.processingStatus ?: "",
                        createdAt = createdResponse.createdAt ?: "",
                        updatedAt = createdResponse.updatedAt ?: "",
                        processedBy = createdResponse.processedBy
                    )
                    dao.insert(createdData)
                    refreshDataFromApi()
                }
            } else {
                println("Error creating data via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data creation. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error creating data: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error creating data: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}") // Cetak body error untuk debugging
            if (e.code() == 401) {
                println("Authentication failed for data creation. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while creating data: ${e.message}")
        }
    }

    // MODIFIKASI: Menerima Uri untuk file
    fun updateDataProcessing(id: Int, data: DataProcessing, fileUri: Uri?) = viewModelScope.launch {
        try {
            // Konversi semua field ke RequestBody
            val projectRequestBody = data.projectId.toRequestBodyOrEmpty()
            val dataSourceDescRequestBody = data.dataSourceDescription.toRequestBodyOrEmpty()
            val processingStepsSummaryRequestBody = data.processingStepsSummary.toRequestBodyOrEmpty()
            val featureEngineeringDetailsRequestBody = data.featureEngineeringDetails.toRequestBodyOrEmpty()
            val processedDataLocationRequestBody = data.processedDataLocation.toRequestBodyOrEmpty()
            val processingStatusRequestBody = data.processingStatus.toRequestBodyOrEmpty()
            val processedByRequestBody = data.processedBy.toRequestBodyOrEmpty()

            // Siapkan bagian file.
            // Jika fileUri null, ini akan mengirim null untuk processedFilePart,
            // yang berarti tidak ada file baru yang diunggah.
            // Django akan mempertahankan file yang ada atau menghapusnya jika field tidak wajib.
            val processedFilePart = prepareFilePart(getApplication(), fileUri)

            val response = DataProcessingRetrofitClient.dataProcessingApiService.updateDataProcessing(
                id = id,
                project = projectRequestBody,
                dataSourceDescription = dataSourceDescRequestBody,
                processingStepsSummary = processingStepsSummaryRequestBody,
                featureEngineeringDetails = featureEngineeringDetailsRequestBody,
                processedDataLocation = processedDataLocationRequestBody,
                processedFile = processedFilePart,
                processingStatus = processingStatusRequestBody,
                processedBy = processedByRequestBody
            )
            if (response.isSuccessful) {
                response.body()?.let { updatedResponse ->
                    val updatedData = DataProcessing(
                        id = updatedResponse.id,
                        projectName = updatedResponse.projectDetail?.name ?: "Unknown Project",
                        projectId = updatedResponse.projectDetail?.id ?: 0,
                        dataSourceDescription = updatedResponse.dataSourceDescription ?: "",
                        processingStepsSummary = updatedResponse.processingStepsSummary ?: "",
                        featureEngineeringDetails = updatedResponse.featureEngineeringDetails ?: "",
                        processedDataLocation = updatedResponse.processedDataLocation ?: "",
                        processedFile = updatedResponse.processedFile ?: "",
                        processingStatus = updatedResponse.processingStatus ?: "",
                        createdAt = updatedResponse.createdAt ?: "",
                        updatedAt = updatedResponse.updatedAt ?: "",
                        processedBy = updatedResponse.processedBy
                    )
                    dao.update(updatedData)
                    refreshDataFromApi()
                }
            } else {
                println("Error updating data via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data update. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error updating data: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error updating data: ${e.code()} - ${e.message()}")
            println("Response body: ${e.response()?.errorBody()?.string()}") // Cetak body error untuk debugging
            if (e.code() == 401) {
                println("Authentication failed for data update. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while updating data: ${e.message}")
        }
    }

    fun deleteDataProcessing(data: DataProcessing) = viewModelScope.launch {
        try {
            val response = DataProcessingRetrofitClient.dataProcessingApiService.deleteDataProcessing(data.id)
            if (response.isSuccessful) {
                dao.delete(data)
                refreshDataFromApi()
            } else {
                println("Error deleting data via API: ${response.code()} - ${response.errorBody()?.string()}")
                if (response.code() == 401) {
                    println("Authentication failed for data deletion. Check if the hardcoded token is valid.")
                }
            }
        } catch (e: IOException) {
            println("Network error deleting data: ${e.message}")
        } catch (e: HttpException) {
            println("HTTP error deleting data: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                println("Authentication failed for data deletion. Check if the hardcoded token is valid.")
            }
        } catch (e: Exception) {
            println("An unexpected error occurred while deleting data: ${e.message}")
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DataProcessingViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return DataProcessingViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
