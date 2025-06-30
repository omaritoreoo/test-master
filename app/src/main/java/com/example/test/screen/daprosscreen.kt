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
import com.example.test.viewmodel.DataProcessingViewModel
import com.example.test.viewmodel.ProjectViewModel
import com.example.test.data.DataProcessing
import com.example.test.data.DataProcessingRequest
import com.example.test.data.Project
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

@Composable
fun DataProcessingScreen(
    navController: NavHostController,
    projectViewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(LocalContext.current.applicationContext as Application)
    ),
    dataProcessingViewModel: DataProcessingViewModel = viewModel(
        factory = DataProcessingViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val localProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())

    val projects = remember(localProjects, apiProjects) {
        (localProjects + apiProjects).sortedBy { it.projectName }
    }
    val allData by dataProcessingViewModel.allDataProcessing.observeAsState(emptyList())

    var showForm by remember { mutableStateOf(false) }
    var selectedDataForEdit by remember { mutableStateOf<DataProcessing?>(null) }
    var selectedDataForView by remember { mutableStateOf<DataProcessing?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var dataToDelete by remember { mutableStateOf<DataProcessing?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                selectedDataForEdit = null
                showForm = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745)),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .wrapContentWidth(Alignment.Start)
        ) {
            Text("Buat Entri Pemrosesan Data", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        if (showForm) {
            DataProcessingForm(
                allProjects = projects,
                // MODIFIKASI: onSubmit sekarang menerima Uri? untuk file
                onSubmit = { dataToProcess, isEdit, fileUri ->
                    if (isEdit && selectedDataForEdit != null) {
                        dataProcessingViewModel.updateDataProcessing(selectedDataForEdit!!.id, dataToProcess, fileUri)
                    } else {
                        // Saat membuat data baru, kita juga akan mengirimkan fileUri
                        dataProcessingViewModel.createDataProcessing(dataToProcess, fileUri)
                    }
                    showForm = false
                    selectedDataForEdit = null
                },
                onCancel = {
                    showForm = false
                    selectedDataForEdit = null
                },
                initialData = selectedDataForEdit
            )
        } else {
            if (allData.isNotEmpty()) {
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
                        Text("Sumber Data", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                        Text("Transformasi", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                        Text("Lokasi File", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text("Action", fontWeight = FontWeight.Bold, modifier = Modifier.width(260.dp), textAlign = TextAlign.End)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(allData) { index, data ->
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
                                Text(data.projectName, modifier = Modifier.width(100.dp))
                                Text(data.dataSourceDescription ?: "", modifier = Modifier.width(150.dp))
                                Text(data.processingStepsSummary ?: "", modifier = Modifier.width(150.dp))
                                Text(data.processedDataLocation ?: "", modifier = Modifier.width(120.dp))
                                Text(data.processingStatus ?: "", modifier = Modifier.width(120.dp))

                                Row(
                                    modifier = Modifier.width(260.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { selectedDataForView = data },
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
                                            selectedDataForEdit = data
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
                                            dataToDelete = data
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
                    "Belum ada data pemrosesan.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        if (selectedDataForView != null) {
            Dialog(onDismissRequest = { selectedDataForView = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        val data = selectedDataForView!!

                        Text(
                            "Pemrosesan Data: ${data.projectName}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
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
                                    onValueChange = {},
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

                        DetailViewField("Sumber Data", data.dataSourceDescription)
                        DetailViewField("Transformasi Data", data.processingStepsSummary)
                        DetailViewField("Rekayasa Fitur", data.featureEngineeringDetails)
                        DetailViewField("Lokasi File Diproses", data.processedDataLocation)
                        DetailViewField("Nama File Data Diproses", data.processedFile)
                        DetailViewField("Status Pemrosesan", data.processingStatus)
                        DetailViewField("Dibuat Pada", data.createdAt)
                        DetailViewField("Diperbarui Pada", data.updatedAt)
                    }
                }
            }
        }

        if (showDeleteConfirmationDialog && dataToDelete != null) {
            DeleteConfirmationDialog(
                entry = dataToDelete!!,
                onDeleteConfirm = {
                    dataToDelete?.let { dataProcessingViewModel.deleteDataProcessing(it) }
                    showDeleteConfirmationDialog = false
                    dataToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmationDialog = false
                    dataToDelete = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataProcessingForm(
    allProjects: List<Project>,
    // MODIFIKASI: onSubmit sekarang menerima Uri? untuk file
    onSubmit: (DataProcessing, Boolean, Uri?) -> Unit,
    onCancel: () -> Unit,
    initialData: DataProcessing? = null
) {
    var selectedProject by remember { mutableStateOf(initialData?.let {
        allProjects.find { p -> p.id == it.projectId }
    } ?: allProjects.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var sourceData by remember { mutableStateOf(initialData?.dataSourceDescription ?: "") }
    var transformationSteps by remember { mutableStateOf(initialData?.processingStepsSummary ?: "") }
    var featureEngineering by remember { mutableStateOf(initialData?.featureEngineeringDetails ?: "") }

    // Inisialisasi untuk menampilkan URL dari API jika ada
    var processedFileLocation by remember {
        mutableStateOf(initialData?.processedDataLocation.takeIf { !it.isNullOrEmpty() } ?: initialData?.processedFile ?: "")
    }
    var processedFileName by remember {
        mutableStateOf(initialData?.processedFile ?: "")
    }

    // State untuk menyimpan URI file yang dipilih pengguna
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }


    var processingStatus by remember { mutableStateOf(initialData?.processingStatus ?: "") }

    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it // Simpan URI yang dipilih
            val fileName = getFileName(context, it)
            processedFileName = fileName ?: it.lastPathSegment ?: "Unknown File"
            processedFileLocation = it.toString() // Tampilkan URI lokal di TextField
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Form Pemrosesan Data",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Nama Proyek", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedProject?.projectName ?: "Pilih Proyek",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allProjects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.projectName) },
                        onClick = {
                            selectedProject = project
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(text = "Sumber Data", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = sourceData, onValueChange = { sourceData = it }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        Text(text = "Transformasi Data", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = transformationSteps, onValueChange = { transformationSteps = it }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        Text(text = "Rekayasa Fitur", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = featureEngineering, onValueChange = { featureEngineering = it }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        Text(text = "Lokasi File Diproses (URI Lokal / URL API)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = processedFileLocation,
            onValueChange = { processedFileLocation = it },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text(text = "Nama File Data Diproses (atau URL)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = processedFileName,
                onValueChange = { processedFileName = it },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { pickFileLauncher.launch(arrayOf("*/*")) },
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

        Text(text = "Status Pemrosesan", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = processingStatus, onValueChange = { processingStatus = it }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    onCancel()
                },
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
                    val data = DataProcessing(
                        id = initialData?.id ?: 0,
                        projectName = selectedProject?.projectName ?: "",
                        projectId = selectedProject?.id ?: 0,
                        dataSourceDescription = sourceData,
                        processingStepsSummary = transformationSteps,
                        featureEngineeringDetails = featureEngineering,
                        processedDataLocation = processedFileLocation,
                        processedFile = processedFileName, // Ini akan menjadi nama file lokal/URL dari API
                        processingStatus = processingStatus,
                        createdAt = initialData?.createdAt ?: now,
                        updatedAt = now,
                        processedBy = initialData?.processedBy
                    )
                    // Panggil onSubmit dengan data dan URI file yang dipilih
                    onSubmit(data, initialData != null, selectedFileUri)
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


// Fungsi helper untuk mendapatkan nama file dari Uri
fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                return it.getString(displayNameIndex)
            }
        }
    }
    return null
}

@Composable
fun DeleteConfirmationDialog(
    entry: DataProcessing,
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
                    color = Color(0xFFD87AB2), // pink lembut
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.projectName,
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
