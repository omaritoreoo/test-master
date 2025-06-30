package com.example.test.network

import com.example.test.data.Project
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Path

interface ApiService {
    @GET("projects/")
    suspend fun getProjects(): Response<List<Project>>

    // Jika endpoint untuk detail/edit/delete membutuhkan ID
    @GET("projects/{id}")
    suspend fun getProjectById(@Path("id") id: Int): Response<Project>

    @POST("projects/")
    suspend fun addProject(@Body project: Project): Response<Project>

    @PUT("projects/{id}")
    suspend fun updateProject(@Path("id") id: Int, @Body project: Project): Response<Project>

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: Int): Response<Void>
}