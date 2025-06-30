// MeaningfulObjectiveRetrofitClient.kt
package com.example.test.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object MeaningfulObjectiveRetrofitClient {
    // Corrected BASE_URL to just the domain
    private const val BASE_URL = "https://fabyaanusakti.pythonanywhere.com/"

    // You might want to add logging and timeouts here as well for this specific client
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Or Level.HEADERS, Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: MeaningfulObjectiveApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use the OkHttpClient with logging and timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeaningfulObjectiveApiService::class.java)
    }
}