package com.example.test.data

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(application: Application) {
    private val userDao: UserDao
    private val preferencesManager: PreferencesManager

    init {
        val db = AppDatabase.getDatabase(application) // Menggunakan AppDatabase
        userDao = db.userDao()
        preferencesManager = PreferencesManager(application)
    }

    fun getLoggedInUser(): Flow<User?> = flow {
        val loggedInUserId = preferencesManager.getLoggedInUserId()
        if (loggedInUserId != -1L) {
            val user = userDao.getUserById(loggedInUserId)
            emit(user)
        } else {
            emit(null)
        }
    }

    suspend fun saveUser(user: User) {
        val userId = userDao.insertUser(user)
        preferencesManager.setLoggedInUserId(userId)
    }

    // Fungsi untuk login: memverifikasi dan menandai user sebagai yang login
    // Memanggil getUserByEmailSingle dari DAO
    suspend fun loginUser(email: String, passwordAttempt: String): User? {
        val user = userDao.getUserByEmailSingle(email) // <--- UBAH DI SINI!
        if (user != null && user.password == passwordAttempt) {
            preferencesManager.setLoggedInUserId(user.id)
            return user
        }
        return null
    }

    // Fungsi untuk mendapatkan User sebagai Flow (untuk observasi LiveData)
    // Memanggil getUserByEmailFlow dari DAO
    fun getUserByEmail(email: String): Flow<User?> { // <--- TAMBAHKAN ATAU MODIFIKASI INI!
        return userDao.getUserByEmailFlow(email) // <--- UBAH DI SINI!
    }

    suspend fun clearUserData() {
        preferencesManager.clearLoggedInUserId()
    }
}