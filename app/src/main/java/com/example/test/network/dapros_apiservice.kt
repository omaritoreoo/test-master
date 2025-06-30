package com.example.test.network

import com.example.test.data.DataProcessingResponse // Masih digunakan untuk respons GET
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart // Anotasi ini sangat penting!
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface DataProcessingApiService {

    // GET semua data processing (tetap sama)
    @GET("api-content/data-processings/")
    suspend fun getAllDataProcessing(): Response<List<DataProcessingResponse>>

    // GET data processing berdasarkan ID (tetap sama)
    @GET("api-content/data-processings/{id}/")
    suspend fun getDataProcessingById(@Path("id") id: Int): Response<DataProcessingResponse>

    // POST (membuat) data processing baru dengan unggahan file
    @Multipart // Gunakan multipart/form-data
    @POST("api-content/data-processings/")
    suspend fun createDataProcessing(
        @Part("project") project: RequestBody,
        @Part("data_source_description") dataSourceDescription: RequestBody,
        @Part("processing_steps_summary") processingStepsSummary: RequestBody,
        @Part("feature_engineering_details") featureEngineeringDetails: RequestBody,
        @Part("processed_data_location") processedDataLocation: RequestBody,
        @Part processedFile: MultipartBody.Part?, // Ini untuk file sebenarnya (bisa null)
        @Part("status") processingStatus: RequestBody,
        @Part("processed_by") processedBy: RequestBody? // Bisa null
    ): Response<DataProcessingResponse>

    // PUT (mengupdate penuh) data processing yang sudah ada dengan unggahan file
    @Multipart // Gunakan multipart/form-data
    @PUT("api-content/data-processings/{id}/")
    suspend fun updateDataProcessing(
        @Path("id") id: Int,
        @Part("project") project: RequestBody,
        @Part("data_source_description") dataSourceDescription: RequestBody,
        @Part("processing_steps_summary") processingStepsSummary: RequestBody,
        @Part("feature_engineering_details") featureEngineeringDetails: RequestBody,
        @Part("processed_data_location") processedDataLocation: RequestBody,
        @Part processedFile: MultipartBody.Part?, // Ini untuk file sebenarnya (bisa null)
        @Part("status") processingStatus: RequestBody,
        @Part("processed_by") processedBy: RequestBody? // Bisa null
    ): Response<DataProcessingResponse>


    // DELETE data processing berdasarkan ID (tetap sama)
    @DELETE("api-content/data-processings/{id}/")
    suspend fun deleteDataProcessing(@Path("id") id: Int): Response<Unit>
}
