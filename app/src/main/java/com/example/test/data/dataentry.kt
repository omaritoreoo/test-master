package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ✅ DataEntry Entity
@Entity(tableName = "data_entry")
data class DataEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val problem: String = "",
    val target: String = "",
    val features: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: String = ""
) {
    fun getField(field: String): String {
        return when (field) {
            "problem" -> problem
            "target" -> target
            "features" -> features
            "startDate" -> startDate
            "endDate" -> endDate
            "status" -> status
            else -> ""
        }
    }

    fun copyField(field: String, value: String): DataEntry {
        return when (field) {
            "problem" -> copy(problem = value)
            "target" -> copy(target = value)
            "features" -> copy(features = value)
            "startDate" -> copy(startDate = value)
            "endDate" -> copy(endDate = value)
            "status" -> copy(status = value)
            else -> this
        }
    }
}