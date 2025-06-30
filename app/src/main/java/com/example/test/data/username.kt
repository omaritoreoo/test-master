package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L, // <--- UBAH DARI INT KE LONG
    val email: String,
    val username: String,
    val password: String
)