package com.example.test.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
// Tidak perlu lagi mengimpor LoginRequest dan LoginResponse di sini
// Tidak perlu lagi mengimpor AuthApiService di sini karena tidak akan ada login API

// Objek untuk manajemen token autentikasi (tetap di sini)
object AuthTokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val AUTH_TOKEN_KEY = "auth_token"

    private var appContext: Context? = null

    // --- BARIS UNTUK DEBUGGING: MENGGUNAKAN TOKEN YANG DIBERIKAN ---
    // Pastikan ini adalah token yang valid dari backend Django Anda
    private const val GIVEN_TOKEN = "c528106165fb16d3c777231c395f5d249756b787" // <--- TOKEN ANDA DI SINI
    // --- AKHIR BARIS DEBUGGING ---

    // Metode ini HARUS dipanggil di Application class Anda (MyApplication.kt)
    fun initialize(context: Context) {
        appContext = context.applicationContext
        // Setelah inisialisasi, jika belum ada token yang disimpan, simpan token yang diberikan.
        // Ini memastikan token selalu tersedia untuk AuthentificationInterceptor.
        if (getStoredToken().isNullOrEmpty()) {
            saveAuthToken(GIVEN_TOKEN)
            println("DEBUG: Token yang diberikan telah disimpan ke SharedPreferences.")
        } else {
            println("DEBUG: Token sudah ada di SharedPreferences.")
        }
    }

    fun saveAuthToken(token: String) {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(AUTH_TOKEN_KEY, token)
            ?.apply()
    }

    fun getStoredToken(): String? {
        return appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(AUTH_TOKEN_KEY, null)
    }

    // Metode utama yang akan dipanggil oleh Interceptor untuk mendapatkan token
    fun getAuthToken(): String? {
        return getStoredToken() // Cukup kembalikan token yang tersimpan
    }

    fun clearAuthToken() {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.remove(AUTH_TOKEN_KEY)
            ?.apply()
    }
}

// Interceptor untuk melampirkan token ke header setiap request API
class AuthenticationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = AuthTokenManager.getAuthToken() // Mengambil token dari AuthTokenManager

        if (token != null) {
            // Menambahkan header Authorization dengan format "Token <token>"
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Token $token")
                .build()
            return chain.proceed(authenticatedRequest)
        }
        return chain.proceed(originalRequest) // Lanjutkan tanpa header jika token tidak ada
    }
}

// Objek utama RetrofitClient yang sekarang menyediakan DataProcessingApiService
object DataProcessingRetrofitClient {

    private const val BASE_URL = "https://arlellll.pythonanywhere.com/" // URL dasar API Anda

    // Interceptor untuk logging permintaan dan respons HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY) // Tingkat logging: BODY menampilkan detail lengkap
    }

    // OkHttpClient yang dikonfigurasi dengan interceptor logging dan autentikasi
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)          // Tambahkan logging interceptor
        .addInterceptor(AuthenticationInterceptor()) // <-- PENTING: Tambahkan interceptor autentikasi di sini
        .connectTimeout(30, TimeUnit.SECONDS)        // Waktu timeout untuk koneksi
        .readTimeout(30, TimeUnit.SECONDS)           // Waktu timeout untuk membaca respons
        .writeTimeout(30, TimeUnit.SECONDS)          // Waktu timeout untuk menulis permintaan
        .build()

    // instance lazily-initialized dari DataProcessingApiService
    val dataProcessingApiService: DataProcessingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Menggunakan Gson untuk konversi JSON
            .client(okHttpClient) // Menggunakan OkHttpClient yang sudah dikonfigurasi
            .build()
            .create(DataProcessingApiService::class.java)
    }
}
