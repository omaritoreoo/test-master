// File: app/src/main/test/network/DataEntryApiService.kt
package com.example.test.network

import com.example.test.data.DataEntryRequest
import com.example.test.data.DataEntryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DataEntryApiService {

    @GET("api-content/problem-framings/")
    suspend fun getAllDataEntries(): Response<List<DataEntryResponse>>

    @GET("api-content/problem-framings/{id}/")
    suspend fun getDataEntryById(@Path("id") id: Int): Response<DataEntryResponse>

    @POST("api-content/problem-framings/")
    suspend fun createDataEntry(@Body request: DataEntryRequest): Response<DataEntryResponse>

    @PUT("api-content/problem-framings/{id}/")
    suspend fun updateDataEntry(@Path("id") id: Int, @Body request: DataEntryRequest): Response<DataEntryResponse>

    @DELETE("api-content/problem-framings/{id}/")
    suspend fun deleteDataEntry(@Path("id") id: Int): Response<Unit>
}