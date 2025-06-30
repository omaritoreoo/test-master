// File: app/src/main/test/data/DataEntry.kt (Asumsi Koreksi)
package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "data_entry")
data class DataEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("project_name")
    val projectName: String?, // Dibuat nullable
    @SerializedName("project_id")
    val projectId: Int?, // Dibuat nullable
    @SerializedName("problem_description")
    val problemDescription: String?, // Dibuat nullable
    val target: String?, // Dibuat nullable
    val stock: String?, // Dibuat nullable
    val inflow: String?, // Dibuat nullable
    val outflow: String?, // Dibuat nullable
    @SerializedName("data_needed")
    val dataNeeded: String?, // Dibuat nullable
    @SerializedName("framed_by")
    val framedBy: String?, // Dibuat nullable
    @SerializedName("framed_by_id")
    val framedById: Int?, // Dibuat nullable
    @SerializedName("date_created")
    val dateCreated: String?, // Dibuat nullable
    @SerializedName("created_at")
    val createdAt: String?, // Dibuat nullable
    @SerializedName("updated_at")
    val updatedAt: String? // Dibuat nullable
) {
    // Fungsi helper untuk copy field tertentu
    fun copyField(field: String, value: String?): DataEntry = when (field) { // Menerima String?
        "projectName" -> copy(projectName = value)
        "problemDescription" -> copy(problemDescription = value)
        "target" -> copy(target = value)
        "stock" -> copy(stock = value)
        "inflow" -> copy(inflow = value)
        "outflow" -> copy(outflow = value)
        "dataNeeded" -> copy(dataNeeded = value)
        "framedBy" -> copy(framedBy = value)
        "dateCreated" -> copy(dateCreated = value)
        else -> this
    }
}