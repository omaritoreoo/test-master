// File: app/src/main/test/data/ProjectRepository.kt
package com.example.test.data

import com.example.test.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map // Tambahkan import ini jika Anda ingin memfilter
import retrofit2.Response

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val apiService: ApiService, // Ini yang saya asumsikan ada di RetrofitClient Anda
    private val dataEntryDao: DataEntryDao,
    private val dataProcessingDao: DataProcessingDao,
    private val modelTrainingDao: ModelTrainingDao
) {

    // --- Fungsi untuk mendapatkan semua proyek lokal (dari Room) ---
    // Ini akan mengembalikan Flow dari semua proyek yang ada di DB lokal
    fun getAllLocalProjects(): Flow<List<Project>> {
        return projectDao.getAllLocalProjects() // Memanggil fungsi dari ProjectDao
    }

    // --- Fungsi untuk mendapatkan semua proyek dari API ---
    // Ini akan melakukan panggilan API dan menyimpan hasilnya ke DB lokal,
    // lalu mengembalikan proyek yang berhasil diambil dari API.
    fun getApiProjectsAndCache(): Flow<List<Project>> = flow {
        try {
            val apiResponse = apiService.getProjects()
            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { projects ->
                    projects.forEach { project ->
                        project.isFromApi = true // Tandai bahwa proyek ini dari API
                        projectDao.insert(project) // Simpan ke DB lokal
                    }
                    emit(projects) // Emit data dari API
                } ?: emit(emptyList()) // Jika body null, emit list kosong
            } else {
                println("API fetch failed: ${apiResponse.code()} - ${apiResponse.errorBody()?.string()}")
                emit(emptyList()) // Emit list kosong atau error state jika diperlukan
            }
        } catch (e: Exception) {
            println("Network error fetching API projects: ${e.message}")
            emit(emptyList()) // Emit list kosong saat ada error jaringan
        }
    }


    // Mendapatkan detail lengkap satu proyek (Project + DataEntry + DataProcessing + ModelTraining)
    suspend fun getProjectDetails(projectId: Int): ProjectDetails? {
        var project: Project? = null

        // Coba ambil proyek dari API terlebih dahulu
        try {
            val apiServiceResponse = apiService.getProjectById(projectId)
            if (apiServiceResponse.isSuccessful) {
                apiServiceResponse.body()?.let { apiProject ->
                    apiProject.isFromApi = true
                    projectDao.insert(apiProject)
                    project = apiProject
                }
            }
        } catch (e: Exception) {
            println("Error fetching project by ID from API: ${e.message}")
        }

        // Jika tidak ditemukan di API atau error, ambil dari lokal
        if (project == null) {
            project = projectDao.getProjectById(projectId)
        }

        return project?.let { actualProject ->
            val dataEntry = dataEntryDao.getProblemFramingByProjectName(actualProject.projectName)
            val dataProcessing = dataProcessingDao.getDataProcessingByProjectName(actualProject.projectName)
            val modelTraining = modelTrainingDao.getModelTrainingByProjectName(actualProject.projectName)

            ProjectDetails(actualProject, dataEntry, dataProcessing, modelTraining)
        } ?: run {
            println("Project with ID $projectId not found in API or local DB.")
            null
        }
    }

    // --- Operasi CRUD untuk Project itu sendiri ---
    suspend fun insertLocalProject(project: Project) {
        projectDao.insert(project.copy(isFromApi = false))
    }

    suspend fun updateLocalProject(project: Project) {
        projectDao.update(project.copy(isFromApi = false))
    }

    suspend fun deleteLocalProject(project: Project) {
        projectDao.delete(project)
    }

    suspend fun addProjectToApi(project: Project): Response<Project> {
        val response = apiService.addProject(project.copy(id = 0, isFromApi = true)) // ID 0 untuk POST baru
        if (response.isSuccessful) {
            response.body()?.let { newProject ->
                projectDao.insert(newProject.copy(isFromApi = true)) // Simpan respons API ke lokal
            }
        }
        return response
    }

    suspend fun updateProjectInApi(project: Project): Response<Project> {
        val response = apiService.updateProject(project.id, project.copy(isFromApi = true))
        if (response.isSuccessful) {
            response.body()?.let { updatedProject ->
                projectDao.update(updatedProject.copy(isFromApi = true)) // Perbarui lokal
            }
        }
        return response
    }

    suspend fun deleteProjectFromApi(projectId: Int): Response<Void> {
        val response = apiService.deleteProject(projectId)
        if (response.isSuccessful) {
            val projectToDelete = projectDao.getProjectById(projectId)
            if (projectToDelete != null) {
                projectDao.delete(projectToDelete) // Hapus dari lokal jika ada
            }
        }
        return response
    }

    // --- Operasi CRUD untuk DataEntry melalui ProjectRepository ---
    suspend fun insertDataEntry(data: DataEntry): Long {
        return dataEntryDao.insert(data)
    }

    suspend fun updateDataEntry(data: DataEntry) {
        dataEntryDao.update(data)
    }

    suspend fun deleteDataEntry(data: DataEntry) {
        dataEntryDao.delete(data)
    }

    fun getAllDataEntries(): Flow<List<DataEntry>> {
        return dataEntryDao.getAll()
    }

    // --- Operasi CRUD untuk DataProcessing melalui ProjectRepository ---
    suspend fun insertDataProcessing(data: DataProcessing) {
        dataProcessingDao.insert(data)
    }

    suspend fun updateDataProcessing(data: DataProcessing) {
        dataProcessingDao.update(data)
    }

    suspend fun deleteDataProcessing(data: DataProcessing) {
        dataProcessingDao.delete(data)
    }

    fun getAllDataProcessing(): Flow<List<DataProcessing>> {
        return dataProcessingDao.getAllDataProcessing()
    }

    // --- Operasi CRUD untuk ModelTraining melalui ProjectRepository ---
    suspend fun insertModelTraining(training: ModelTraining) {
        modelTrainingDao.insert(training)
    }

    suspend fun updateModelTraining(training: ModelTraining) {
        modelTrainingDao.update(training)
    }

    suspend fun deleteModelTraining(training: ModelTraining) {
        modelTrainingDao.delete(training)
    }

    fun getAllModelTrainings(): Flow<List<ModelTraining>> {
        return modelTrainingDao.getAll()
    }
    fun getCombinedProjects(): Flow<List<Project>> = combine(
        getAllLocalProjects(), // Flow dari proyek lokal
        getApiProjectsAndCache() // Flow dari proyek API (akan memicu fetch dan cache)
    ) { localProjects, apiProjects ->
        // Logika penggabungan dan filtering
        val uniqueLocalProjects = localProjects.filter { localProject ->
            // Pastikan proyek lokal tidak diduplikasi jika ID dan isFromApi sama
            apiProjects.none { apiProject -> apiProject.id == localProject.id && apiProject.isFromApi }
        }
        (uniqueLocalProjects + apiProjects).sortedBy { it.projectName }
    }
}