package com.example.test.network

import com.example.test.data.Project // <--- Impor Project
import com.example.test.data.ProjectWithMeaningfulObjectives
import retrofit2.Response
import retrofit2.http.GET

interface MeaningfulObjectiveApiService {
    @GET("api/meaningful_objectives-only/")
    suspend fun getMeaningfulObjectives(): Response<List<Project>> // <-- Ini penting!
    @GET("api/meaningful_objectives-only/") // Sesuaikan dengan endpoint yang benar yang mengembalikan JSON Anda
    suspend fun getProjectsWithMeaningfulObjectives(): Response<List<ProjectWithMeaningfulObjectives>>
}