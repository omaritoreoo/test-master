package com.example.test.data

data class ProjectDetails(
    val project: Project,
    val dataEntry: DataEntry?,
    val dataProcessing: DataProcessing?,
    val modelTraining: ModelTraining?
)