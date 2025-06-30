// File: app/src/main/test/network/RetrofitClient.kt
package com.example.test.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



// Objek utama RetrofitClient yang sekarang menyediakan semua API Services
object DataRequestRetrofitClient {

    private const val BASE_URL = "https://arlellll.pythonanywhere.com/" // URL dasar API Anda

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(AuthenticationInterceptor()) // Interceptor autentikasi
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instansiasi ApiService (untuk ProjectRepository)
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    // Instansiasi DataProcessingApiService
    val dataProcessingApiService: DataProcessingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(DataProcessingApiService::class.java)
    }

    // Instansiasi ModelTrainingApiService
    val modelTrainingApiService: ModelTrainingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ModelTrainingApiService::class.java)
    }

    // Instansiasi DataEntryApiService (untuk Problem Framing)
    val dataEntryApiService: DataEntryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(DataEntryApiService::class.java)
    }

    // Instansiasi DatasetRequestApiService (BARU)
    val datasetRequestApiService: DatasetRequestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(DatasetRequestApiService::class.java)
    }
}
