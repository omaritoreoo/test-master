package com.example.test.screen

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.viewmodel.ModelTrainingViewModel
import com.example.test.viewmodel.ProjectViewModel
import com.example.test.viewmodel.DataProcessingViewModel
import com.example.test.data.ModelTraining
import com.example.test.data.Project
import com.example.test.data.DataProcessing
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable // Import for clickable modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainModelScreen(
    navController: NavHostController,
    modelTrainingViewModel: ModelTrainingViewModel = viewModel(
        factory = ModelTrainingViewModel.Factory(LocalContext.current.applicationContext as Application)
    ),
    projectViewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(LocalContext.current.applicationContext as Application)
    ),
    dataProcessingViewModel: DataProcessingViewModel = viewModel(
        factory = DataProcessingViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val allModelTrainings by modelTrainingViewModel.allModelTrainings.observeAsState(emptyList())
    val allLocalProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())
    val allProjects = remember(allLocalProjects, apiProjects) {
        (allLocalProjects + apiProjects).sortedBy { it.projectName }
    }
    val allDataProcessings by dataProcessingViewModel.allDataProcessing.observeAsState(emptyList())

    var showForm by remember { mutableStateOf(false) }
    var selectedTrainingForEdit by remember { mutableStateOf<ModelTraining?>(null) }
    var selectedTrainingForView by remember { mutableStateOf<ModelTraining?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var trainingToDelete by remember { mutableStateOf<ModelTraining?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                selectedTrainingForEdit = null
                showForm = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745)),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .wrapContentWidth(Alignment.Start)
        ) {
            Text("Buat Entri Pelatihan Model", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        if (showForm) {
            TrainModelForm(
                allProjects = allProjects,
                allDataProcessings = allDataProcessings,
                onSubmit = { training, fileUri -> // Mengambil fileUri dari form
                    if (selectedTrainingForEdit != null) {
                        modelTrainingViewModel.updateModelTraining(selectedTrainingForEdit!!.id, training, fileUri)
                    } else {
                        modelTrainingViewModel.createModelTraining(training, fileUri)
                    }
                    showForm = false
                    selectedTrainingForEdit = null
                },
                onCancel = {
                    showForm = false
                    selectedTrainingForEdit = null
                },
                initialData = selectedTrainingForEdit
            )
        } else {
            if (allModelTrainings.isNotEmpty()) {
                val sharedHorizontalScrollState = rememberScrollState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(sharedHorizontalScrollState)
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text("#", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                        Text("Proyek", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text("Nama Model", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                        Text("Performa", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text("Aksi", fontWeight = FontWeight.Bold, modifier = Modifier.width(260.dp), textAlign = TextAlign.End)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(allModelTrainings) { index, training ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clipToBounds(),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(sharedHorizontalScrollState)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text((index + 1).toString(), modifier = Modifier.width(40.dp))
                                Text(training.projectName ?: "", modifier = Modifier.width(100.dp))
                                Text(training.modelName ?: "", modifier = Modifier.width(150.dp))
                                Text(training.performance ?: "", modifier = Modifier.width(100.dp))
                                Text(training.performanceAfterRefinement ?: "", modifier = Modifier.width(100.dp)) // Menggunakan performanceAfterRefinement sebagai status utama

                                Row(
                                    modifier = Modifier.width(260.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { selectedTrainingForView = training },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6c757d)),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(36.dp)
                                    ) {
                                        Text("View", fontSize = 12.sp, color = Color.White)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            selectedTrainingForEdit = training
                                            showForm = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff)),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(36.dp)
                                    ) {
                                        Text("Edit", fontSize = 12.sp, color = Color.White)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            trainingToDelete = training
                                            showDeleteConfirmationDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFdc3545)),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(36.dp)
                                    ) {
                                        Text("Delete", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Belum ada data pelatihan model.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        // Tampilan Dialog untuk View
        if (selectedTrainingForView != null) {
            Dialog(onDismissRequest = { selectedTrainingForView = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val training = selectedTrainingForView!!

                        Text(
                            "Detail Pelatihan Model: ${training.modelName}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        @Composable
                        fun DetailViewField(label: String, value: String?) {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)) {
                                Text(label, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = value ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        disabledContainerColor = Color.White,
                                        focusedBorderColor = Color.Gray,
                                        unfocusedBorderColor = Color.Gray,
                                        disabledBorderColor = Color.Gray,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        disabledTextColor = Color.Black
                                    )
                                )
                            }
                        }

                        DetailViewField("Proyek", training.projectName)
                        DetailViewField("Nama Model", training.modelName)
                        DetailViewField("Tipe Model", training.modelType)
                        DetailViewField("Algoritma", training.algorithm)
                        DetailViewField("Data Pelatihan Digunakan", training.trainingData)
                        DetailViewField("Dilatih Oleh", training.trainedByUsername)
                        DetailViewField("Performa (Akurasi)", training.performance)
                        DetailViewField("Path Model", training.modelPath)
                        DetailViewField("Strategi Penyempurnaan", training.refinementStrategy)
                        DetailViewField("Status Penyempurnaan", training.performanceAfterRefinement)
                        DetailViewField("Tanggal Pelatihan", training.trainingDate)
                        DetailViewField("Dibuat Pada", training.createdAt)
                        DetailViewField("Diperbarui Pada", training.updatedAt)

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedTrainingForView = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tutup", color = Color.White)
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmationDialog && trainingToDelete != null) {
            DeleteConfirmationDialogModel(
                entry = trainingToDelete!!,
                onDeleteConfirm = {
                    trainingToDelete?.let { modelTrainingViewModel.deleteModelTraining(it) }
                    showDeleteConfirmationDialog = false
                    trainingToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmationDialog = false
                    trainingToDelete = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainModelForm(
    allProjects: List<Project>,
    allDataProcessings: List<DataProcessing>,
    onSubmit: (ModelTraining, Uri?) -> Unit,
    onCancel: () -> Unit,
    initialData: ModelTraining? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedProject by remember { mutableStateOf(initialData?.let {
        allProjects.find { p -> p.projectName == it.projectName }
    } ?: allProjects.firstOrNull()) }
    var expandedProject by remember { mutableStateOf(false) }

    var selectedTrainingData by remember { mutableStateOf(initialData?.let {
        allDataProcessings.find { dp -> dp.id == it.trainingDataUsedId }
    } ?: allDataProcessings.firstOrNull()) }
    var expandedTrainingData by remember { mutableStateOf(false) }

    var selectedTrainedBy by remember { mutableStateOf(initialData?.trainedById ?: 1) }

    var modelName by remember { mutableStateOf(initialData?.modelName ?: "") }
    var algorithm by remember { mutableStateOf(initialData?.algorithm ?: "") }
    var trainingDataDisplay by remember { mutableStateOf(initialData?.trainingData ?: "") }
    var performanceDisplay by remember { mutableStateOf(initialData?.performance ?: "") }
    var modelPathDisplay by remember { mutableStateOf(initialData?.modelPath ?: "") }
    var refinementStrategy by remember { mutableStateOf(initialData?.refinementStrategy ?: "") }
    var trainingDate by remember { mutableStateOf(initialData?.trainingDate ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).format(Date())) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    // --- Pilihan untuk Dropdown modelType ---
    val modelTypeOptions = listOf("classification", "regression", "clustering", "other") // GANTI DENGAN PILIHAN NYATA DARI DJANGO ANDA
    var selectedModelType by remember { mutableStateOf(initialData?.modelType ?: modelTypeOptions.first()) }
    var expandedModelType by remember { mutableStateOf(false) }

    // --- Pilihan untuk Dropdown refiningStatus ---
    val refiningStatusOptions = listOf("completed", "in_progress", "failed", "pending") // GANTI DENGAN PILIHAN NYATA DARI DJANGO ANDA
    var selectedRefiningStatus by remember { mutableStateOf(initialData?.performanceAfterRefinement ?: refiningStatusOptions.first()) }
    var expandedRefiningStatus by remember { mutableStateOf(false) }


    LaunchedEffect(initialData) {
        initialData?.modelPath?.let { path ->
            if (path.isNotEmpty()) {
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    modelPathDisplay = path
                    selectedFileUri = null
                } else {
                    try {
                        val uri = Uri.parse(path)
                        selectedFileUri = uri
                        val fileName = getFileName(context, uri)
                        modelPathDisplay = fileName ?: uri.lastPathSegment ?: path
                    } catch (e: Exception) {
                        modelPathDisplay = path
                        selectedFileUri = null
                    }
                }
            } else {
                modelPathDisplay = ""
                selectedFileUri = null
            }
        }
    }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileName(context, it)
            modelPathDisplay = fileName ?: it.lastPathSegment ?: "Unknown File"
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF0F0F0))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Form Pelatihan Model",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Project Dropdown
        Text(text = "Nama Proyek", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedProject,
            onExpandedChange = { expandedProject = !expandedProject }
        ) {
            OutlinedTextField(
                value = selectedProject?.projectName ?: "Pilih Proyek",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProject) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedProject,
                onDismissRequest = { expandedProject = false }
            ) {
                allProjects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.projectName) },
                        onClick = {
                            selectedProject = project
                            expandedProject = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Training Data Used Dropdown
        Text(text = "Data Pelatihan Digunakan", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedTrainingData,
            onExpandedChange = { expandedTrainingData = !expandedTrainingData }
        ) {
            OutlinedTextField(
                value = selectedTrainingData?.dataSourceDescription ?: "Pilih Data Proses",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTrainingData) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedTrainingData,
                onDismissRequest = { expandedTrainingData = false }
            ) {
                allDataProcessings.forEach { dataProcess ->
                    DropdownMenuItem(
                        text = { Text(dataProcess.dataSourceDescription ?: "No Description") },
                        onClick = {
                            selectedTrainingData = dataProcess
                            trainingDataDisplay = dataProcess.dataSourceDescription ?: ""
                            expandedTrainingData = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Nama Model
        Text(text = "Nama Model", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = modelName, onValueChange = { modelName = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // Tipe Model Dropdown
        Text(text = "Tipe Model", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedModelType,
            onExpandedChange = { expandedModelType = !expandedModelType }
        ) {
            OutlinedTextField(
                value = selectedModelType,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedModelType) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedModelType,
                onDismissRequest = { expandedModelType = false }
            ) {
                modelTypeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedModelType = option
                            expandedModelType = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Algoritma
        Text(text = "Algoritma", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = algorithm, onValueChange = { algorithm = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // Performa Model (hanya Akurasi yang diwakili oleh field 'performance')
        Text(text = "Performa Model (Akurasi)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = performanceDisplay, onValueChange = { performanceDisplay = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // File Model Terlatih (dengan fungsi upload)
        Text(text = "File Model Terlatih (URL / URI Lokal)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = modelPathDisplay,
                onValueChange = { modelPathDisplay = it },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF007bff), shape = MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Pilih File Model",
                    tint = Color.White
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Refinement Details
        Text(text = "Strategi Penyempurnaan", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = refinementStrategy, onValueChange = { refinementStrategy = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // Status Penyempurnaan Dropdown
        Text(text = "Status Penyempurnaan", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedRefiningStatus,
            onExpandedChange = { expandedRefiningStatus = !expandedRefiningStatus }
        ) {
            OutlinedTextField(
                value = selectedRefiningStatus,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRefiningStatus) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedRefiningStatus,
                onDismissRequest = { expandedRefiningStatus = false }
            ) {
                refiningStatusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedRefiningStatus = option
                            expandedRefiningStatus = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Training Date
        Text(text = "Tanggal Pelatihan (YYYY-MM-DDTHH:MM:SS.SSSSSSZ)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = trainingDate, onValueChange = { trainingDate = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Return", color = Color.White)
            }
            Button(
                onClick = {
                    val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).format(Date())
                    val training = ModelTraining(
                        id = initialData?.id ?: 0,
                        projectName = selectedProject?.projectName,
                        projectId = selectedProject?.id, // Pastikan ini ada di ModelTraining entitas Anda
                        modelName = modelName,
                        modelType = selectedModelType, // Menggunakan nilai dari dropdown
                        algorithm = algorithm,
                        trainingData = trainingDataDisplay,
                        performance = performanceDisplay,
                        modelPath = selectedFileUri?.toString() ?: initialData?.modelPath,
                        refinementStrategy = refinementStrategy,
                        performanceAfterRefinement = selectedRefiningStatus, // Menggunakan nilai dari dropdown
                        trainingDataUsedId = selectedTrainingData?.id,
                        trainedById = selectedTrainedBy,
                        trainedByUsername = null,
                        trainingDate = trainingDate,
                        createdAt = initialData?.createdAt ?: now,
                        updatedAt = now
                    )
                    onSubmit(training, selectedFileUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Save Changes", color = Color.White)
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialogModel(
    entry: ModelTraining,
    onDeleteConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Apakah Ingin menghapus ?",
                    color = Color(0xFFD87AB2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.modelName ?: "Model",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC66)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Return", color = Color.White)
                    }
                    Button(
                        onClick = {
                            onDeleteConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F6F)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
