// File: app/src/main/java/com/example/test/screen/RequestDatasetScreen.kt
package com.example.test.screen

import android.annotation.SuppressLint
import android.app.Application
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.data.DatasetRequest
import com.example.test.data.Project
import com.example.test.viewmodel.DatasetRequestViewModel
import com.example.test.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RequestDatasetScreen(
    navController: NavController,
    datasetViewModel: DatasetRequestViewModel = viewModel(
        factory = DatasetRequestViewModel.Factory(LocalContext.current.applicationContext as Application)
    ),
    projectViewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current

    // State untuk form (sekarang sebagai DatasetRequest itu sendiri)
    val selectedRequestForForm = remember { mutableStateOf<DatasetRequest?>(null) }
    // Gunakan state ini untuk mengontrol tampilan antara daftar dan form
    val showFormScreen = remember { mutableStateOf(false) }

    // Data dari ViewModel
    val datasetRequests by datasetViewModel.allDatasetRequests.collectAsState(initial = emptyList())
    val isLoading by datasetViewModel.isLoading.collectAsState()
    val errorMessage by datasetViewModel.error.collectAsState()

    val localProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())

    val projectList = remember(localProjects, apiProjects) {
        (localProjects + apiProjects).distinctBy { it.id }.sortedBy { it.projectName.orEmpty() }
    }
    val projectNames = remember(projectList) {
        projectList.map { it.projectName.orEmpty() }
    }

    var query by remember { mutableStateOf("") }

    // AnimatedContent untuk transisi antara daftar dan form
    AnimatedContent(
        targetState = showFormScreen.value,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(700)) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(700))
        }, label = "Screen Transition"
    ) { showForm ->
        if (showForm) {
            // Tampilkan form dalam layar penuh
            selectedRequestForForm.value?.let { request ->
                DatasetRequestForm(
                    request = request,
                    onRequestChange = { field, value ->
                        if (field == "projectName") {
                            val project = projectList.find { it.projectName == value }
                            selectedRequestForForm.value = selectedRequestForForm.value!!.copy(
                                projectName = value,
                                projectId = project?.id,
                                projectDescription = project?.description
                            )
                        } else if (field == "numFeatures") {
                            selectedRequestForForm.value = selectedRequestForForm.value!!.copyIntField(field, value?.toIntOrNull())
                        } else {
                            selectedRequestForForm.value = selectedRequestForForm.value!!.copyField(field, value)
                        }
                    },
                    onSave = {
                        val req = selectedRequestForForm.value!!
                        if (req.projectName.isNullOrBlank() || req.descriptionProblem.isNullOrBlank() ||
                            req.targetForDataset.isNullOrBlank() || req.typeDataNeeded.isNullOrBlank() ||
                            req.startDateNeeded.isNullOrBlank() || req.endDateNeeded.isNullOrBlank()) {
                            Toast.makeText(context, "Harap lengkapi semua bidang yang wajib diisi!", Toast.LENGTH_SHORT).show()
                            return@DatasetRequestForm
                        }

                        if (req.id == 0) {
                            datasetViewModel.addDatasetRequest(req)
                        } else {
                            datasetViewModel.updateDatasetRequest(req)
                        }
                        showFormScreen.value = false // Kembali ke daftar setelah menyimpan
                    },
                    onClose = { showFormScreen.value = false }, // Kembali ke daftar
                    projectList = projectNames
                )
            }
        } else {
            // Tampilkan daftar permintaan dataset
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Bagian atas: Judul, Tombol Tambah, dan Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Daftar Permintaan Dataset",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            // Inisialisasi DatasetRequest baru untuk form kosong
                            selectedRequestForForm.value = DatasetRequest(
                                projectName = null,
                                projectId = null,
                                projectDescription = null,
                                requestedByUsername = "Default User", // Sesuaikan dengan user login
                                requestedById = 1, // Sesuaikan dengan ID user login
                                descriptionProblem = null,
                                targetForDataset = null,
                                typeDataNeeded = null,
                                dataProcessingActivity = null,
                                numFeatures = null,
                                datasetSize = null,
                                fileFormat = null,
                                startDateNeeded = null,
                                endDateNeeded = null,
                                status = "Pending", // Default status
                                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                                updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                            )
                            showFormScreen.value = true // Tampilkan form layar penuh
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari Permintaan Dataset...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Tampilan Kondisional: Loading, Error, Data, atau Kosong
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Memuat data...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        Text(
                            text = "Terjadi kesalahan: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { datasetViewModel.fetchAndCacheAllDatasetRequests() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Coba Lagi")
                        }
                    }
                    datasetRequests.isNotEmpty() -> {
                        val filteredRequests = datasetRequests.filter {
                            query.isBlank() ||
                                    (it.projectName?.contains(query, true) == true) ||
                                    (it.descriptionProblem?.contains(query, true) == true) ||
                                    (it.status?.contains(query, true) == true) ||
                                    (it.requestedByUsername?.contains(query, true) == true)
                        }
                        HorizontalScrollableDatasetRequestTable(
                            data = filteredRequests,
                            onEdit = { requestToEdit ->
                                selectedRequestForForm.value = requestToEdit
                                showFormScreen.value = true // Tampilkan form layar penuh
                            },
                            onDelete = { requestToDelete ->
                                datasetViewModel.deleteDatasetRequest(requestToDelete.id)
                            }
                        )
                    }
                    else -> {
                        Text(
                            text = "Tidak ada permintaan dataset yang tersedia.",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetRequestForm(
    request: DatasetRequest,
    onRequestChange: (String, String?) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    projectList: List<String>
) {
    val context = LocalContext.current
    var selectedProjectName by remember(request.projectName) { mutableStateOf(request.projectName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                text = if (request.id == 0) "Tambah Permintaan Dataset" else "Edit Permintaan Dataset",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0006D),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp)) // Untuk menyeimbangkan IconButton
        }
        Spacer(modifier = Modifier.height(16.dp))

        ProjectDropdownField(
            selectedProject = selectedProjectName.orEmpty(),
            onProjectSelected = { newName ->
                selectedProjectName = newName
                onRequestChange("projectName", newName)
            },
            projectList = projectList
        )
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Deskripsi Masalah", request.descriptionProblem) {
            onRequestChange("descriptionProblem", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Target untuk Dataset", request.targetForDataset) {
            onRequestChange("targetForDataset", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Tipe Data yang Dibutuhkan", request.typeDataNeeded) {
            onRequestChange("typeDataNeeded", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Aktivitas Pemrosesan Data", request.dataProcessingActivity) {
            onRequestChange("dataProcessingActivity", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField(
            label = "Jumlah Fitur",
            value = request.numFeatures?.toString(),
            onValueChange = { onRequestChange("numFeatures", it) },
        )
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Ukuran Dataset", request.datasetSize) {
            onRequestChange("datasetSize", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Format File", request.fileFormat) {
            onRequestChange("fileFormat", it)
        }
        Spacer(modifier = Modifier.height(12.dp))

        val startDatePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                    onRequestChange("startDateNeeded", formattedDate)
                },
                (request.startDateNeeded?.substringBefore("-")?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)),
                (request.startDateNeeded?.substringAfter("-")?.substringBefore("-")?.toIntOrNull()?.minus(1) ?: Calendar.getInstance().get(Calendar.MONTH)),
                (request.startDateNeeded?.substringAfterLast("-")?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            )
        }
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text("Tanggal Mulai Dibutuhkan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = request.startDateNeeded.orEmpty(),
                onValueChange = { onRequestChange("startDateNeeded", it) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { startDatePickerDialog.show() },
                trailingIcon = {
                    IconButton(onClick = { startDatePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal Mulai")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val endDatePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                    onRequestChange("endDateNeeded", formattedDate)
                },
                (request.endDateNeeded?.substringBefore("-")?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)),
                (request.endDateNeeded?.substringAfter("-")?.substringBefore("-")?.toIntOrNull()?.minus(1) ?: Calendar.getInstance().get(Calendar.MONTH)),
                (request.endDateNeeded?.substringAfterLast("-")?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            )
        }
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text("Tanggal Akhir Dibutuhkan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = request.endDateNeeded.orEmpty(),
                onValueChange = { onRequestChange("endDateNeeded", it) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { endDatePickerDialog.show() },
                trailingIcon = {
                    IconButton(onClick = { endDatePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal Akhir")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val statusOptions = listOf("Pending", "Approved", "Rejected", "Completed")
        var expandedStatus by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedStatus,
            onExpandedChange = { expandedStatus = !expandedStatus },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = request.status.orEmpty(),
                onValueChange = { },
                label = { Text("Status") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedStatus,
                onDismissRequest = { expandedStatus = false }
            ) {
                statusOptions.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            onRequestChange("status", status)
                            expandedStatus = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LabelAndField("Diminta Oleh", request.requestedByUsername) {
            onRequestChange("requestedByUsername", it)
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.defaultMinSize(minWidth = 100.dp)
            ) {
                Text("Return", color = Color.White)
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.defaultMinSize(minWidth = 140.dp)
            ) {
                Text("Simpan Perubahan", color = Color.White)
            }
        }
    }
}
@Composable
fun HorizontalScrollableDatasetRequestTable(
    data: List<DatasetRequest>,
    onEdit: (DatasetRequest) -> Unit,
    onDelete: (DatasetRequest) -> Unit
    // onView dihapus
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        DatasetRequestTableHeaderRow()
        data.forEachIndexed { index, request ->
            DatasetRequestDataRow(request, index, onEdit, onDelete) // Panggil tanpa onView
        }
    }
}

@Composable
fun DatasetRequestTableHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
    ) {
        TableCell(text = "#", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
        TableCell(text = "Nama Projek", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
        TableCell(text = "Deskripsi Masalah", fontWeight = FontWeight.Bold, modifier = Modifier.width(200.dp))
        TableCell(text = "Tipe Data", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        TableCell(text = "Ukuran Dataset", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        TableCell(text = "Format File", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        TableCell(text = "Status", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        TableCell(text = "Diminta Oleh", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
        TableCell(text = "Aksi", fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp)) // Lebar disesuaikan
    }
}

@Composable
fun DatasetRequestDataRow(
    request: DatasetRequest,
    index: Int,
    onEdit: (DatasetRequest) -> Unit,
    onDelete: (DatasetRequest) -> Unit
    // onView dihapus
) {
    val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFF9F9F9)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        TableCell(text = "${index + 1}", modifier = Modifier.width(60.dp))
        TableCell(text = request.projectName.orEmpty(), modifier = Modifier.width(150.dp))
        TableCell(text = request.descriptionProblem.orEmpty(), modifier = Modifier.width(200.dp))
        TableCell(text = request.typeDataNeeded.orEmpty(), modifier = Modifier.width(120.dp))
        TableCell(text = request.datasetSize.orEmpty(), modifier = Modifier.width(120.dp))
        TableCell(text = request.fileFormat.orEmpty(), modifier = Modifier.width(100.dp))
        TableCell(text = request.status.orEmpty(), modifier = Modifier.width(100.dp))
        TableCell(text = request.requestedByUsername.orEmpty(), modifier = Modifier.width(150.dp))
        TableCell(modifier = Modifier.width(150.dp)) { // Lebar disesuaikan
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol "Lihat" dihapus
                ActionButton("Edit", Color(0xFF88C28C)) { onEdit(request) }
                ActionButton("Hapus", Color(0xFFE38787)) { onDelete(request) } // Panggil onDelete langsung
            }
        }
    }
}

// DatasetRequestDetailView (dialog view) dihapus sepenuhnya dari sini
// DeleteConfirmationDialog (dialog konfirmasi delete) dihapus sepenuhnya dari sini

@Composable
fun LabelValueWithGrayBackground(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = value.ifEmpty { "-" },
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String?,
    onValueChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = { onValueChange(it) },
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (value.isNullOrEmpty()) Text("") },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDropdownField(
    selectedProject: String,
    onProjectSelected: (String) -> Unit,
    projectList: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedProject,
            onValueChange = {},
            label = { Text("Nama Projek") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            projectList.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project) },
                    onClick = {
                        onProjectSelected(project)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TableCell(
    text: String?,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier.width(140.dp)
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        Text(text = text.orEmpty(), fontSize = 14.sp, fontWeight = fontWeight)
    }
}

@Composable
fun TableCell(
    modifier: Modifier = Modifier.width(140.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        content()
    }
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(end = 4.dp)
            .height(32.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}
