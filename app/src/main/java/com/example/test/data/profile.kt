package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val username: String, // This will be the username from signup
    val firstName: String = "",
    val lastName: String = "",
    val emailAddress: String, // This will be the email from signup
    val photoUri: String? = null, // URI to the profile photo
    val bio: String = "",
    val title: String = "" // Added 'title' field
)