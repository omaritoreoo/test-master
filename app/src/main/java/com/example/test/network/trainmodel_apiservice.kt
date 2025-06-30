// File: app/src/main/test/network/ModelTrainingApiService.kt
package com.example.test.network

import com.example.test.data.ModelTrainingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ModelTrainingApiService {

    @GET("api-content/training-models/") // Sesuaikan dengan endpoint API Anda
    suspend fun getAllModelTrainings(): Response<List<ModelTrainingResponse>>

    @GET("api-content/training-models/{id}/")
    suspend fun getModelTrainingById(@Path("id") id: Int): Response<ModelTrainingResponse>

    @Multipart // Penting untuk unggahan file (model_path)
    @POST("api-content/training-models/")
    suspend fun createModelTraining(
        @Part("project") project: RequestBody,
        @Part("training_data_used") trainingDataUsed: RequestBody?, // Bisa null
        @Part("trained_by") trainedBy: RequestBody?, // Bisa null
        @Part("model_name") modelName: RequestBody,
        @Part("model_type") modelType: RequestBody,
        @Part("algorithm_used") algorithmUsed: RequestBody,
        @Part("model_performance") modelPerformance: RequestBody, // Kirim JSON string dari Map
        @Part modelPath: MultipartBody.Part?, // File model itu sendiri
        @Part("refining_strategy") refiningStrategy: RequestBody,
        @Part("refining_status") refiningStatus: RequestBody,
        @Part("training_date") trainingDate: RequestBody // Tanggal pelatihan
    ): Response<ModelTrainingResponse>

    @Multipart // Penting untuk unggahan file
    @PUT("api-content/training-models/{id}/")
    suspend fun updateModelTraining(
        @Path("id") id: Int,
        @Part("project") project: RequestBody,
        @Part("training_data_used") trainingDataUsed: RequestBody?,
        @Part("trained_by") trainedBy: RequestBody?,
        @Part("model_name") modelName: RequestBody,
        @Part("model_type") modelType: RequestBody,
        @Part("algorithm_used") algorithmUsed: RequestBody,
        @Part("model_performance") modelPerformance: RequestBody, // Kirim JSON string dari Map
        @Part modelPath: MultipartBody.Part?, // File model itu sendiri
        @Part("refining_strategy") refiningStrategy: RequestBody,
        @Part("refining_status") refiningStatus: RequestBody,
        @Part("training_date") trainingDate: RequestBody
    ): Response<ModelTrainingResponse>

    @DELETE("api-content/training-models/{id}/")
    suspend fun deleteModelTraining(@Path("id") id: Int): Response<Unit>
}