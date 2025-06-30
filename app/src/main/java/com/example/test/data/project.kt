// In your com.example.test.data package
package com.example.test.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @SerializedName("name")
    var projectName: String = "",
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("status")
    var status: String? = "Pending",
    @SerializedName("created_by")
    var createdBy: String? = null,
    @SerializedName("start_date")
    var startDate: String? = null,
    @SerializedName("end_date")
    var endDate: String? = null,
    @SerializedName("supervisor")
    var clientName: String? = null,
    @SerializedName("location")
    var location: String? = null,
    var isFromApi: Boolean = false,

    @Ignore
    @SerializedName("meaningful_objectives")
    var meaningfulObjectives: MeaningfulObjectives? = null
)