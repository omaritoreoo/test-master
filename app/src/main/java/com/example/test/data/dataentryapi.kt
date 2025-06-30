package com.example.test.data

import com.google.gson.annotations.SerializedName

// Model untuk objek bersarang 'framed_by_detail'
data class FramedByDetail(
    val id: Int,
    val username: String?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val email: String?
)

// Model utama untuk respons API Problem Framing (Data Entry)
data class DataEntryResponse(
    val id: Int,
    @SerializedName("project_detail")
    val projectDetail: ProjectDetail?,
    @SerializedName("framed_by_detail")
    val framedByDetail: FramedByDetail?,
    @SerializedName("problem_description")
    val problemDescription: String?,
    @SerializedName("target_goal")
    val targetGoal: String?,
    @SerializedName("stock_initial_state")
    val stockInitialState: String?,
    @SerializedName("input_inflow_description")
    val inputInflowDescription: String?,
    @SerializedName("output_outflow_description")
    val outputOutflowDescription: String?,
    @SerializedName("key_features_data")
    val keyFeaturesData: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("project")
    val project: Int?,
    @SerializedName("framed_by")
    val framedBy: Int?
)

// Model untuk mengirim data ke API (POST/PUT)
data class DataEntryRequest(
    @SerializedName("project") val project: Int?,
    @SerializedName("framed_by") val framedBy: Int?,
    @SerializedName("problem_description") val problemDescription: String?,
    @SerializedName("target_goal") val targetGoal: String?,
    @SerializedName("stock_initial_state") val stockInitialState: String?,
    @SerializedName("input_inflow_description") val inputInflowDescription: String?,
    @SerializedName("output_outflow_description") val outputOutflowDescription: String?,
    @SerializedName("key_features_data") val keyFeaturesData: String?
)