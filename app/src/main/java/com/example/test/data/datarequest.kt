// File: app/src/main/test/data/DatasetRequest.kt
package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "dataset_request")
data class DatasetRequest(
    @PrimaryKey(autoGenerate = false) val id: Int = 0, // ID dari API
    val projectName: String?, // Nama proyek dari project_detail
    val projectId: Int?, // ID proyek dari project_detail
    val projectDescription: String?, // Deskripsi proyek dari project_detail
    val requestedByUsername: String?, // Username dari requested_by_detail
    val requestedById: Int?, // ID user yang request
    @SerializedName("description_problem")
    val descriptionProblem: String?, // description_problem (sebelumnya descriptionn)
    @SerializedName("target_for_dataset")
    val targetForDataset: String?, // target_for_dataset (sebelumnya target)
    @SerializedName("type_data_needed")
    val typeDataNeeded: String?, // type_data_needed (sebelumnya dataType)
    @SerializedName("data_processing_activity")
    val dataProcessingActivity: String?, // data_processing_activity (sebelumnya dataProcessing)
    @SerializedName("num_features")
    val numFeatures: Int?, // num_features (sebelumnya featureCount)
    @SerializedName("dataset_size")
    val datasetSize: String?, // dataset_size
    @SerializedName("file_format")
    val fileFormat: String?, // file_format (sebelumnya expectedFileFormat)
    @SerializedName("start_date_needed")
    val startDateNeeded: String?, // start_date_needed (sebelumnya startDate)
    @SerializedName("end_date_needed")
    val endDateNeeded: String?, // end_date_needed (sebelumnya endDate)
    val status: String?,
    val createdAt: String?, // created_at dari API
    val updatedAt: String? // updated_at dari API
) {
    // Fungsi helper untuk copy field tertentu (untuk form UI)
    fun copyField(field: String, value: String?): DatasetRequest = when (field) {
        "projectName" -> copy(projectName = value)
        "projectDescription" -> copy(projectDescription = value)
        "descriptionProblem" -> copy(descriptionProblem = value)
        "targetForDataset" -> copy(targetForDataset = value)
        "typeDataNeeded" -> copy(typeDataNeeded = value)
        "dataProcessingActivity" -> copy(dataProcessingActivity = value)
        "datasetSize" -> copy(datasetSize = value)
        "fileFormat" -> copy(fileFormat = value)
        "startDateNeeded" -> copy(startDateNeeded = value)
        "endDateNeeded" -> copy(endDateNeeded = value)
        "status" -> copy(status = value)
        "requestedByUsername" -> copy(requestedByUsername = value)
        else -> this
    }

    // Fungsi helper untuk copy field Int
    fun copyIntField(field: String, value: Int?): DatasetRequest = when (field) {
        "numFeatures" -> copy(numFeatures = value)
        "projectId" -> copy(projectId = value)
        "requestedById" -> copy(requestedById = value)
        else -> this
    }
}
