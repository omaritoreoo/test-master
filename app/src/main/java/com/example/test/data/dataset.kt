// com/example/test/data/DatasetReply.kt
package com.example.test.data

import com.google.gson.annotations.SerializedName

data class DatasetReply(
    @SerializedName("project_name") val projectName: String?,
    @SerializedName("message_text") val messageText: String?,
    @SerializedName("dataset_link") val datasetLink: String?,
    @SerializedName("created_at") val createdAt: String?
)