package com.example.test.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.data.*
import com.example.test.network.ApiService
import com.example.test.network.RetrofitClient
import com.example.test.utils.PdfGenerator // Pastikan PdfGenerator ada dan berfungsi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val appDatabase = AppDatabase.getDatabase(application)
    private val projectDao = appDatabase.projectDao()
    private val dataEntryDao = appDatabase.dataEntryDao()
    private val dataProcessingDao = appDatabase.dataProcessingDao()
    private val modelTrainingDao = appDatabase.modelTrainingDao()
    private val apiService = RetrofitClient.apiService

    // Inisialisasi ProjectRepository dengan semua dependensi DAO dan ApiService
    private val repository: ProjectRepository =
        ProjectRepository(projectDao, apiService, dataEntryDao, dataProcessingDao, modelTrainingDao)

    private val applicationContext: Context = application.applicationContext

    // --- State untuk UI (LiveData untuk proyek, sesuai permintaan Anda) ---
    val allLocalProjects: LiveData<List<Project>> = repository.getAllLocalProjects().asLiveData()
    val apiProjects: LiveData<List<Project>> = repository.getApiProjectsAndCache().asLiveData() // Ini akan memicu fetch API dan caching

    // State lainnya tetap StateFlow
    private val _selectedProjectDetails = MutableStateFlow<ProjectDetails?>(null)
    val selectedProjectDetails: StateFlow<ProjectDetails?> = _selectedProjectDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _pdfDownloadUri = MutableStateFlow<Uri?>(null)
    val pdfDownloadUri: StateFlow<Uri?> = _pdfDownloadUri.asStateFlow()

    init {
        // Pemicu awal untuk mengambil data dari API saat ViewModel dibuat
        // Ini akan mengisi apiProjects dan juga cache ke DB lokal.
        // allLocalProjects akan mengamati DB lokal.
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // CollectLatest di sini hanya untuk memicu Flow dan caching di repository.
                // Data akan mengalir ke apiProjects LiveData.
                repository.getApiProjectsAndCache().collectLatest {
                    // Tidak perlu melakukan apa-apa di sini, LiveData sudah terupdate
                }
            } catch (e: Exception) {
                _error.value = "Error during initial API fetch: ${e.message}"
                Log.e("ProjectViewModel", "Error during initial API fetch", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk me-refresh data API secara manual
    fun refreshApiProjects() { // Dulu namanya fetchApiProjects
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ini akan memicu pengambilan data API terbaru dan memperbarui apiProjects LiveData
                repository.getApiProjectsAndCache().collectLatest {
                    // Data akan otomatis mengalir ke apiProjects LiveData
                }
            } catch (e: Exception) {
                _error.value = "Error refreshing API projects: ${e.message}"
                Log.e("ProjectViewModel", "Error refreshing API projects", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Mengubah nama fungsi dari getProjectDetails menjadi fetchProjectDetails agar lebih jelas bahwa itu adalah operasi pengambilan data.
    fun fetchProjectDetails(projectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val details = repository.getProjectDetails(projectId)
                _selectedProjectDetails.value = details
            } catch (e: Exception) {
                _error.value = "Failed to load project details: ${e.message}"
                Log.e("ProjectViewModel", "Error loading project details for ID $projectId", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk menghapus pilihan proyek dan kembali ke daftar
    fun clearSelectedProject() {
        _selectedProjectDetails.value = null
        _error.value = null // Bersihkan error juga saat kembali ke daftar
        _pdfDownloadUri.value = null // Bersihkan uri PDF juga
    }

    fun downloadProjectDetailsAsPdf(projectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _pdfDownloadUri.value = null
            try {
                val details = repository.getProjectDetails(projectId)
                if (details != null) {
                    val pdfGenerator = PdfGenerator(applicationContext)
                    val pdfFile = pdfGenerator.generateProjectPdf(
                        details.project,
                        details.dataEntry,
                        details.dataProcessing,
                        details.modelTraining
                    )
                    if (pdfFile != null) {
                        val uri = FileProvider.getUriForFile(
                            applicationContext,
                            "${applicationContext.packageName}.provider",
                            pdfFile
                        )
                        _pdfDownloadUri.value = uri
                    } else {
                        _error.value = "Failed to generate PDF file. PDF file is null."
                        Log.e("ProjectViewModel", "PdfGenerator returned null file.")
                    }
                } else {
                    _error.value = "Project details not found for PDF generation."
                    Log.e("ProjectViewModel", "Project details are null for PDF generation.")
                }
            } catch (e: Exception) {
                _error.value = "Error generating PDF: ${e.message}"
                Log.e("ProjectViewModel", "Error generating PDF for ID $projectId", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProjectToApi(project: Project) { // Nama diubah untuk lebih jelas
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.addProjectToApi(project)
                if (response.isSuccessful) {
                    refreshApiProjects() // Refresh API projects setelah successful add
                } else {
                    _error.value = "Failed to add project to API: ${response.message()}"
                    Log.e("ProjectViewModel", "Failed to add project to API: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.value = "Network error adding project: ${e.message}"
                Log.e("ProjectViewModel", "Network error adding project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProjectInApi(project: Project) { // Nama diubah untuk lebih jelas
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (project.id != 0) {
                    val response = repository.updateProjectInApi(project)
                    if (response.isSuccessful) {
                        refreshApiProjects() // Refresh API projects setelah successful update
                    } else {
                        _error.value = "Failed to update project in API: ${response.message()}"
                        Log.e("ProjectViewModel", "Failed to update project in API: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } else {
                    _error.value = "Cannot update project: Project ID is invalid."
                    Log.w("ProjectViewModel", "Cannot update project: Project ID is invalid (ID: ${project.id}).")
                }
            } catch (e: Exception) {
                _error.value = "Network error updating project: ${e.message}"
                Log.e("ProjectViewModel", "Network error updating project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProjectFromApi(project: Project) { // Nama diubah untuk lebih jelas
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (project.id != 0) {
                    val response = repository.deleteProjectFromApi(project.id)
                    if (response.isSuccessful) {
                        refreshApiProjects() // Refresh API projects setelah successful delete
                    } else {
                        _error.value = "Failed to delete project from API: ${response.message()}"
                        Log.e("ProjectViewModel", "Failed to delete project from API: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } else {
                    _error.value = "Cannot delete project: Project ID is invalid."
                    Log.w("ProjectViewModel", "Cannot delete project: Project ID is invalid (ID: ${project.id}).")
                }
            } catch (e: Exception) {
                _error.value = "Network error deleting project: ${e.message}"
                Log.e("ProjectViewModel", "Network error deleting project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- CRUD untuk Project (yang ini hanya berinteraksi dengan Room DB lokal) ---
    fun addLocalProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.insertLocalProject(project)
                // allLocalProjects LiveData akan otomatis diperbarui karena mengamati DB
            } catch (e: Exception) {
                _error.value = "Error adding local project: ${e.message}"
                Log.e("ProjectViewModel", "Error adding local project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLocalProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.updateLocalProject(project)
                // allLocalProjects LiveData akan otomatis diperbarui
            } catch (e: Exception) {
                _error.value = "Error updating local project: ${e.message}"
                Log.e("ProjectViewModel", "Error updating local project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLocalProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.deleteLocalProject(project)
                // allLocalProjects LiveData akan otomatis diperbarui
            } catch (e: Exception) {
                _error.value = "Error deleting local project: ${e.message}"
                Log.e("ProjectViewModel", "Error deleting local project", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- CRUD untuk DataEntry (melalui ProjectRepository) ---
    fun addDataEntry(data: DataEntry) = viewModelScope.launch {
        repository.insertDataEntry(data)
    }
    fun updateDataEntry(data: DataEntry) = viewModelScope.launch {
        repository.updateDataEntry(data)
    }
    fun deleteDataEntry(data: DataEntry) = viewModelScope.launch {
        repository.deleteDataEntry(data)
    }
    val allDataEntries: StateFlow<List<DataEntry>> = repository.getAllDataEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    // --- CRUD untuk DataProcessing (melalui ProjectRepository) ---
    fun addDataProcessing(data: DataProcessing) = viewModelScope.launch {
        repository.insertDataProcessing(data)
    }
    fun updateDataProcessing(data: DataProcessing) = viewModelScope.launch {
        repository.updateDataProcessing(data)
    }
    fun deleteDataProcessing(data: DataProcessing) = viewModelScope.launch {
        repository.deleteDataProcessing(data)
    }
    val allDataProcessing: StateFlow<List<DataProcessing>> = repository.getAllDataProcessing()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    // --- CRUD untuk ModelTraining (melalui ProjectRepository) ---
    fun addModelTraining(training: ModelTraining) = viewModelScope.launch {
        repository.insertModelTraining(training)
    }
    fun updateModelTraining(training: ModelTraining) = viewModelScope.launch {
        repository.updateModelTraining(training)
    }
    fun deleteModelTraining(training: ModelTraining) = viewModelScope.launch {
        repository.deleteModelTraining(training)
    }
    val allModelTrainings: StateFlow<List<ModelTraining>> = repository.getAllModelTrainings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    // Factory untuk instansiasi ViewModel
    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ProjectViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}