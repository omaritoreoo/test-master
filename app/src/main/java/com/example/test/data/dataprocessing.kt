package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "data_processing")
data class DataProcessing(
    @PrimaryKey // ID akan datang dari API, jadi tidak autoGenerate
    val id: Int,
    val projectName: String, // Nama proyek dari project_detail.name (untuk Room)
    val projectId: Int, // ID proyek dari project_detail.id (untuk Room)

    @SerializedName("data_source_description")
    val dataSourceDescription: String?,
    @SerializedName("processing_steps_summary")
    val processingStepsSummary: String?,
    @SerializedName("feature_engineering_details")
    val featureEngineeringDetails: String?,
    @SerializedName("processed_data_location")
    val processedDataLocation: String?,
    @SerializedName("processed_file")
    val processedFile: String?, // URL file yang sudah diproses
    @SerializedName("status")
    val processingStatus: String?, // Status pemrosesan data
    @SerializedName("created_at")
    val createdAt: String?, // Waktu entri ini dibuat (dari API)
    @SerializedName("updated_at")
    val updatedAt: String?, // Waktu terakhir diupdate (dari API)
    @SerializedName("processed_by")
    val processedBy: Int? // ID pengguna yang memproses
)

data class DataProcessingResponse(
    val id: Int,
    @SerializedName("project_detail")
    val projectDetail: ProjectDetail?,
    @SerializedName("data_source_description")
    val dataSourceDescription: String?,
    @SerializedName("processing_steps_summary")
    val processingStepsSummary: String?,
    @SerializedName("feature_engineering_details")
    val featureEngineeringDetails: String?,
    @SerializedName("processed_data_location")
    val processedDataLocation: String?,
    @SerializedName("processed_file")
    val processedFile: String?,
    @SerializedName("status")
    val processingStatus: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("processed_by")
    val processedBy: Int?
)

data class DataProcessingRequest(
    @SerializedName("project") val project: Int,
    @SerializedName("data_source_description") val dataSourceDescription: String?,
    @SerializedName("processing_steps_summary") val processingStepsSummary: String?,
    @SerializedName("feature_engineering_details") val featureEngineeringDetails: String?,
    @SerializedName("processed_data_location") val processedDataLocation: String?,
    @SerializedName("processed_file") val processedFile: String? = null, // Ini sudah benar untuk Skenario A
    @SerializedName("status") val processingStatus: String? = "in_progress", // Pastikan casing sesuai Django
    @SerializedName("processed_by") val processedBy: Int? = null
)
// Class untuk project_detail yang bersarang dalam DataProcessingResponse
data class ProjectDetail(
    val id: Int,
    @SerializedName("external_id")
    val externalId: String?,
    val name: String?,
    val description: String?,
    val location: String?,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String?,
    val status: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)