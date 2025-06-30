package com.example.test.screen

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.viewmodel.ProjectViewModel
import com.example.test.data.Project
import com.example.test.data.ProjectDetails
import com.example.test.utils.PdfGenerator // Import PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(
    navController: NavController,
    viewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val localProjects by viewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by viewModel.apiProjects.observeAsState(emptyList())

    val combinedProjects = remember(localProjects, apiProjects) {
        val uniqueLocalProjects = localProjects.filter { localProject ->
            apiProjects.none { apiProject -> apiProject.id == localProject.id && apiProject.isFromApi }
        }
        (uniqueLocalProjects + apiProjects).sortedBy { it.projectName }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedProjectDetails by viewModel.selectedProjectDetails.collectAsState()

    val context = LocalContext.current
    val pdfGenerator = remember { PdfGenerator(context) } // Inisialisasi PdfGenerator

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedProjectDetails == null) "My Projects" else "Project Details") },
                navigationIcon = {
                    if (selectedProjectDetails != null) {
                        IconButton(onClick = { viewModel.clearSelectedProject() })
                        {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to list")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            error?.let {
                Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
            }

            if (selectedProjectDetails == null) {
                if (combinedProjects.isEmpty() && !isLoading && error == null) {
                    Text("No projects found. Pull down to refresh or check your connection.")
                } else {
                    LazyColumn {
                        items(combinedProjects) { project ->
                            ProjectListItem(project = project) {
                                viewModel.fetchProjectDetails(project.id)
                            }
                        }
                    }
                }
            } else {
                ProjectDetailContent(
                    projectDetails = selectedProjectDetails!!,
                    onDownloadPdf = {
                        val pdfFile = pdfGenerator.generateProjectPdf(
                            project = selectedProjectDetails!!.project,
                            dataEntry = selectedProjectDetails!!.dataEntry,
                            dataProcessing = selectedProjectDetails!!.dataProcessing,
                            modelTraining = selectedProjectDetails!!.modelTraining
                        )

                        pdfFile?.let { file ->
                            // Berhasil membuat PDF, sekarang bagikan
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider", // Authority konsisten dengan AndroidManifest.xml
                                file
                            )

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            // Pastikan ada aplikasi yang bisa menangani intent ini
                            if (shareIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(Intent.createChooser(shareIntent, "Share PDF using"))
                                Toast.makeText(context, "PDF generated and ready to share", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_LONG).show()
                            }
                            Log.d("PDF_GEN", "PDF saved to: ${file.absolutePath}")

                        } ?: run {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            Log.e("PDF_GEN", "Failed to generate PDF file.")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectListItem(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = project.projectName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project.description ?: "No description", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${project.status}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Source: ${if (project.isFromApi) "API" else "Local"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProjectDetailContent(
    projectDetails: ProjectDetails,
    onDownloadPdf: () -> Unit
) {
    // Smart cast issue resolved here because projectDetails is non-null
    // by the time it reaches this Composable.
    val project = projectDetails.project
    val dataEntry = projectDetails.dataEntry
    val dataProcessing = projectDetails.dataProcessing
    val modelTraining = projectDetails.modelTraining

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Project Core Details ---
        Text("Project Name: ${project.projectName}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Description: ${project.description ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Status: ${project.status ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Created By: ${project.createdBy ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Start Date: ${project.startDate ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("End Date: ${project.endDate ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Client Name: ${project.clientName ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Location: ${project.location ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Source: ${if (project.isFromApi) "API" else "Local"}", style = MaterialTheme.typography.bodySmall)

        // --- Data Entry Details ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("--- Problem Framing Details ---", style = MaterialTheme.typography.titleMedium)
        dataEntry?.let { entry ->
            Text("Problem Description: ${entry.problemDescription}", style = MaterialTheme.typography.bodySmall)
            Text("Target: ${entry.target}", style = MaterialTheme.typography.bodySmall)
            Text("Stock: ${entry.stock}", style = MaterialTheme.typography.bodySmall)
            Text("Inflow: ${entry.inflow}", style = MaterialTheme.typography.bodySmall)
            Text("Outflow: ${entry.outflow}", style = MaterialTheme.typography.bodySmall)
            Text("Data Needed: ${entry.dataNeeded}", style = MaterialTheme.typography.bodySmall)
            Text("Framed By: ${entry.framedBy}", style = MaterialTheme.typography.bodySmall)
            Text("Date Created: ${entry.dateCreated}", style = MaterialTheme.typography.bodySmall)
        } ?: Text("Data Entry: Not available", style = MaterialTheme.typography.bodySmall)


        // --- Data Processing Details ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("--- Data Processing Details ---", style = MaterialTheme.typography.titleMedium)
        dataProcessing?.let { processing ->
            Text("Source Data: ${processing.dataSourceDescription ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Transformation Steps: ${processing.processingStepsSummary ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Feature Engineering: ${processing.featureEngineeringDetails ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Processed File Location: ${processing.processedDataLocation ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Processed File: ${processing.processedFile ?: "N/A"}", style = MaterialTheme.typography.bodySmall) // Ini sekarang adalah URL/nama file yang diproses
            Text("Processing Status: ${processing.processingStatus ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Created At: ${processing.createdAt ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        } ?: Text("Data Processing: Not available", style = MaterialTheme.typography.bodySmall)


        // --- Model Training Details ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("--- Model Training Details ---", style = MaterialTheme.typography.titleMedium)
        modelTraining?.let { training ->
            Text("Model Name: ${training.modelName}", style = MaterialTheme.typography.bodySmall)
            Text("Model Type: ${training.modelType}", style = MaterialTheme.typography.bodySmall)
            Text("Algorithm: ${training.algorithm}", style = MaterialTheme.typography.bodySmall)
            Text("Training Data: ${training.trainingData}", style = MaterialTheme.typography.bodySmall)
            Text("Performance: ${training.performance}", style = MaterialTheme.typography.bodySmall)
            Text("Model Path: ${training.modelPath}", style = MaterialTheme.typography.bodySmall)
            Text("Refinement Strategy: ${training.refinementStrategy}", style = MaterialTheme.typography.bodySmall)
            Text("Performance After Refinement: ${training.performanceAfterRefinement}", style = MaterialTheme.typography.bodySmall)
        } ?: Text("Model Training: Not available", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDownloadPdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download as PDF")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Details as PDF")
        }
    }
}