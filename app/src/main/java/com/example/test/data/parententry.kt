package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_entries")
data class ParentEntry(
    @PrimaryKey val id: String,
    val description: String,
    val target: String,
    val features: String,
    val startDate: String,
    val endDate: String,
    val status: String
)
