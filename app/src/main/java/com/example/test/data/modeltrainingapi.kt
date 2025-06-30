package com.example.test.data

import com.google.gson.annotations.SerializedName

// Model untuk objek bersarang 'model_performance'
data class ModelPerformance(
    val accuracy: Double?,
    @SerializedName("f1_score")
    val f1Score: Double?,
    val recall: Double?,
    @SerializedName("acuracy") // Jika backend Anda memang mengirim "acuracy" (typo), pertahankan ini
    val accuracyTypo: Double?
)

// Model untuk objek bersarang 'trained_by_detail'
data class TrainedByDetail(
    val id: Int,
    val username: String?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val email: String?
)

// Model utama untuk respons API Model Training
data class ModelTrainingResponse(
    val id: Int,
    @SerializedName("project_detail")
    val projectDetail: ProjectDetail?,
    @SerializedName("training_data_used_detail")
    val trainingDataUsedDetail: DataProcessingResponse?,
    @SerializedName("trained_by_detail")
    val trainedByDetail: TrainedByDetail?,
    @SerializedName("model_name")
    val modelName: String?,
    @SerializedName("model_type")
    val modelType: String?,
    @SerializedName("algorithm_used")
    val algorithmUsed: String?,
    @SerializedName("model_performance")
    val modelPerformance: ModelPerformance?,
    @SerializedName("model_path")
    val modelPath: String?, // Ini akan menjadi URL file di API
    @SerializedName("refining_strategy")
    val refiningStrategy: String?,
    @SerializedName("refining_status")
    val refiningStatus: String?,
    @SerializedName("training_date")
    val trainingDate: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("training_data_used")
    val trainingDataUsed: Int?, // Foreign key ID saja
    @SerializedName("trained_by")
    val trainedBy: Int? // Foreign key ID saja
)

data class ModelTrainingRequest(
    @SerializedName("project") val project: Int,
    @SerializedName("training_data_used") val trainingDataUsed: Int?,
    @SerializedName("trained_by") val trainedBy: Int?,
    @SerializedName("model_name") val modelName: String?,
    @SerializedName("model_type") val modelType: String?,
    @SerializedName("algorithm_used") val algorithmUsed: String?,
    @SerializedName("model_performance") val modelPerformance: Map<String, Any>?, // Kirim sebagai Map untuk JSON
    @SerializedName("refining_strategy") val refiningStrategy: String?,
    @SerializedName("refining_status") val refiningStatus: String?,
    @SerializedName("training_date") val trainingDate: String?
)