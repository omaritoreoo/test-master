package com.example.test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test.data.DatasetReply
import com.example.test.network.DatasetRetrofitClient
import kotlinx.coroutines.launch

class AllDatasetRepliesViewModel(application: Application) : AndroidViewModel(application) {

    private val _datasetReplies = MutableLiveData<List<DatasetReply>?>()
    val datasetReplies: LiveData<List<DatasetReply>?> = _datasetReplies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchAllDatasetReplies()
    }

    fun fetchAllDatasetReplies() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Panggil endpoint yang sekarang mengembalikan objek tunggal
                val response = DatasetRetrofitClient.apiServiceDataset.getSingleDatasetReply() // <-- Perubahan di sini
                if (response.isSuccessful) {
                    val singleDataset = response.body()
                    // Jika server mengembalikan objek tunggal, bungkus dalam daftar
                    _datasetReplies.value = if (singleDataset != null) listOf(singleDataset) else emptyList() // <-- Perubahan di sini
                    Log.d("AllDatasetVM", "Successfully fetched single dataset reply and converted to list. Size: ${if (singleDataset != null) 1 else 0} item")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.value = "Failed to fetch datasets: ${response.code()} - $errorBody"
                    _datasetReplies.value = null
                    Log.e("AllDatasetVM", "Failed to fetch single dataset reply: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                _datasetReplies.value = null
                Log.e("AllDatasetVM", "Network error fetching dataset (expected single object):", e) // Log pesan yang lebih spesifik
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi deleteDatasetReply (jika ada, tidak relevan untuk error ini)
    // ...

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AllDatasetRepliesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AllDatasetRepliesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}