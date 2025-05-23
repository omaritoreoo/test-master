package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey val id: Int = 1,  // Hanya 1 profile simpan saja
    val name: String,
    val dateOfBirth: String,
    val region: String,
    val country: String,
    val mobile: String,
    val photoUri: String?
)
