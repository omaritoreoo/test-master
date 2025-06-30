package com.example.test.data

import com.google.gson.annotations.SerializedName

// Model untuk project_detail di dalam respons API
data class ProjectDetailApi(
    val id: Int,
    @SerializedName("supervisor_detail")
    val supervisorDetail: Map<String, Any>?, // Gunakan Map<String, Any> untuk fleksibilitas
    @SerializedName("external_id")
    val externalId: String?,
    @SerializedName("name")
    val name: String?,
    val description: String?,
    val location: String?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    val status: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

// Model untuk requested_by_detail di dalam respons API
data class RequestedByDetailApi(
    val id: Int,
    val username: String?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val email: String?
)

// Model utama untuk respons API Dataset Request
data class DatasetRequestApiResponse(
    val id: Int,
    @SerializedName("project_detail")
    val projectDetail: ProjectDetailApi?,
    @SerializedName("requested_by_detail")
    val requestedByDetail: RequestedByDetailApi?,
    @SerializedName("description_problem")
    val descriptionProblem: String?,
    @SerializedName("target_for_dataset")
    val targetForDataset: String?,
    @SerializedName("type_data_needed")
    val typeDataNeeded: String?,
    @SerializedName("data_processing_activity")
    val dataProcessingActivity: String?,
    @SerializedName("num_features")
    val numFeatures: Int?,
    @SerializedName("dataset_size")
    val datasetSize: String?,
    @SerializedName("file_format")
    val fileFormat: String?,
    @SerializedName("start_date_needed")
    val startDateNeeded: String?,
    @SerializedName("end_date_needed")
    val endDateNeeded: String?,
    val status: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

// Model untuk request POST/PUT ke API (jika diperlukan)
// Ini bisa disesuaikan jika API Anda membutuhkan format yang berbeda untuk request
data class DatasetRequestApiRequest(
    @SerializedName("project")
    val project: Int?, // ID proyek terkait
    @SerializedName("requested_by")
    val requestedBy: Int?, // ID user yang request
    @SerializedName("description_problem")
    val descriptionProblem: String?,
    @SerializedName("target_for_dataset")
    val targetForDataset: String?,
    @SerializedName("type_data_needed")
    val typeDataNeeded: String?,
    @SerializedName("data_processing_activity")
    val dataProcessingActivity: String?,
    @SerializedName("num_features")
    val numFeatures: Int?,
    @SerializedName("dataset_size")
    val datasetSize: String?,
    @SerializedName("file_format")
    val fileFormat: String?,
    @SerializedName("start_date_needed")
    val startDateNeeded: String?,
    @SerializedName("end_date_needed")
    val endDateNeeded: String?,
    val status: String?
)
