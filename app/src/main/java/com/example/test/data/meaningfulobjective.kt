package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "meaningful_objectives")
data class MeaningfulObjectives(
    @PrimaryKey(autoGenerate = true)
    var moId: Int = 0, // HARUS var dan punya nilai default

    var projectId: Int = 0, // HARUS var dan punya nilai default. Ini adalah FK ke Project.id

    @SerializedName("objective_name")
    var objectiveName: String? = null,
    @SerializedName("organizational")
    var organizational: String? = null, // HARUS var dan punya nilai default null
    @SerializedName("leading_indicators")
    var leadingIndicators: String? = null, // HARUS var dan punya nilai default null
    @SerializedName("user_outcomes")
    var userOutcomes: String? = null, // HARUS var dan punya nilai default null
    @SerializedName("model_properties")
    var modelProperties: String? = null // HARUS var dan punya nilai default null
)