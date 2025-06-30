package com.example.test.network

import com.example.test.data.DatasetReply
import retrofit2.Response
import retrofit2.http.GET

interface ApiServiceDataset {
    @GET("/api/dataset-reply/")
    suspend fun getSingleDatasetReply(): Response<DatasetReply>
}