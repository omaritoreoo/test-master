package com.example.test.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // Metode untuk mengambil User tunggal secara asinkron (misal untuk login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailSingle(email: String): User? // <-- NAMA BARU

    // Metode untuk mengamati User secara real-time (mengembalikan Flow)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmailFlow(email: String): Flow<User?> // <-- NAMA BARU

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}