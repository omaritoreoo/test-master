// File: app/src/main/test/data/ModelTraining.kt
package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// import com.google.gson.annotations.SerializedName // Tidak diperlukan di sini karena ini untuk Room, bukan API langsung

@Entity(tableName = "model_training")
data class ModelTraining(
    @PrimaryKey // ID akan datang dari API, jadi tidak autoGenerate
    val id: Int,
    val projectName: String?, // Nama proyek dari project_detail.name di API
    val projectId: Int?, // ID proyek dari project_detail.id di API (penting untuk POST/PUT)
    val modelName: String?,
    val modelType: String?,
    val algorithm: String?,
    val trainingData: String?, // Akan diisi dari training_data_used_detail.data_source_description
    val performance: String?, // Akan diisi dari model_performance.accuracy (sebagai string)
    val modelPath: String?, // URL file model dari API atau URI lokal untuk unggahan
    val refinementStrategy: String?,
    val performanceAfterRefinement: String?, // Akan diisi dari refining_status
    val trainingDataUsedId: Int?, // ID dari DataProcessing yang digunakan (Foreign Key)
    val trainedById: Int?, // ID user yang melatih (Foreign Key)
    val trainedByUsername: String?, // Username user yang melatih (untuk tampilan)
    val trainingDate: String?, // Tanggal pelatihan dari API
    val createdAt: String?,
    val updatedAt: String?
)
