package com.example.test.network


import com.example.test.data.DatasetRequestApiRequest
import com.example.test.data.DatasetRequestApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DatasetRequestApiService {
    @GET("api-content/dataset-requests/") // Asumsi endpoint ini
    suspend fun getAllDatasetRequests(): Response<List<DatasetRequestApiResponse>>

    @GET("api-content/dataset-requests/{id}/")
    suspend fun getDatasetRequestById(@Path("id") id: Int): Response<DatasetRequestApiResponse>

    @POST("api-content/dataset-requests/")
    suspend fun createDatasetRequest(@Body request: DatasetRequestApiRequest): Response<DatasetRequestApiResponse>

    @PUT("api-content/dataset-requests/{id}/")
    suspend fun updateDatasetRequest(@Path("id") id: Int, @Body request: DatasetRequestApiRequest): Response<DatasetRequestApiResponse>

    @DELETE("api-content/dataset-requests/{id}/")
    suspend fun deleteDatasetRequest(@Path("id") id: Int): Response<Unit>
}
