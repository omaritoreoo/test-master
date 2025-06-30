package com.example.test.screen

import android.app.Application
import android.widget.DatePicker
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.data.Project
import com.example.test.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

@Composable
fun contentColorFor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    navController: NavHostController? = null,
    projectViewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val localProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gabungkan proyek lokal dan API, lalu urutkan
    val combinedProjects = remember(localProjects, apiProjects) {
        // Buat set untuk melacak ID proyek API yang sudah ada
        val apiProjectIds = apiProjects.map { it.id }.toSet()

        // Filter proyek lokal: hanya masukkan yang ID-nya tidak ada di API atau yang isFromApi-nya false (benar-benar lokal)
        val uniqueLocalProjects = localProjects.filter { localProject ->
            !localProject.isFromApi || !apiProjectIds.contains(localProject.id)
        }
        (uniqueLocalProjects + apiProjects).sortedBy { it.projectName }
    }


    var showForm by remember { mutableStateOf(false) }
    var projectId by remember { mutableStateOf<Int?>(null) }
    var projectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    val statusOptions = listOf("Pending", "Done")
    var isCurrentProjectFromApi by remember { mutableStateOf(false) } // Menandakan apakah proyek yang sedang diedit berasal dari API

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    fun resetForm() {
        projectId = null
        projectName = ""
        description = ""
        location = ""
        supervisor = ""
        startDate = ""
        endDate = ""
        status = "Pending"
        isCurrentProjectFromApi = false // Reset status ini juga
    }

    val tableHeaderBackgroundColor = Color(0xFFF0F0F0)
    val returnButtonColor = Color(0xFF4285F4)
    val saveChangesButtonColor = Color(0xFF4CAF50)
    val createProjectButtonColor = Color(0xFF4CAF50)
    val editButtonColor = createProjectButtonColor
    val deleteButtonColor = Color(0xFFE57373)
    val statusOngoingBgColor = Color(0xFFE3F2FD)
    val statusOngoingTextColor = Color(0xFF2196F3)
    val statusCompleteBgColor = Color(0xFFE8F5E9)
    val statusCompleteTextColor = Color(0xFF4CAF50)
    val uploadToApiButtonColor = Color(0xFFFFA000) // Warna baru untuk tombol upload ke API

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (showForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Nama Proyek",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    CustomInputField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        // Nama proyek dari API TIDAK lagi readOnly, karena bisa diupdate
                        // readOnly = isCurrentProjectFromApi
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Deskripsi",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    CustomInputField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        // readOnly = isCurrentProjectFromApi
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Lokasi",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    CustomInputField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        // readOnly = isCurrentProjectFromApi
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Supervisor",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    CustomInputField(
                        value = supervisor,
                        onValueChange = { supervisor = it },
                        modifier = Modifier.fillMaxWidth(),
                        // readOnly = isCurrentProjectFromApi
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Tanggal Mulai",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                            CustomInputField(
                                value = startDate,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showStartDatePicker = true }, // Selalu bisa diklik
                                trailingIcon = {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal Mulai")
                                    }
                                }
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Tanggal Berakhir",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                            CustomInputField(
                                value = endDate,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEndDatePicker = true }, // Selalu bisa diklik
                                trailingIcon = {
                                    IconButton(onClick = { showEndDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal Berakhir")
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    StatusDropdown(
                        selectedStatus = status,
                        onStatusSelected = { status = it },
                        statusOptions = statusOptions,
                        modifier = Modifier.fillMaxWidth(),
                        // Status proyek dari API TIDAK lagi readOnly
                        // enabled = !isCurrentProjectFromApi
                    )
                    Spacer(Modifier.height(32.dp))

                    // Tombol Aksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                resetForm()
                                showForm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = returnButtonColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f)
                                .padding(horizontal = 4.dp) // Sesuaikan padding
                        ) {
                            Text("Kembali", color = Color.White)
                        }

                        // Logika tombol "Simpan/Perbarui"
                        Button(
                            onClick = {
                                if (projectName.isNotBlank()) {
                                    val project = Project(
                                        id = projectId ?: 0,
                                        projectName = projectName,
                                        description = description,
                                        location = location,
                                        clientName = supervisor,
                                        startDate = startDate,
                                        endDate = endDate,
                                        status = status,
                                        createdBy = "Admin",
                                        // isFromApi sekarang diatur berdasarkan apakah proyek ini aslinya dari API atau tidak
                                        isFromApi = isCurrentProjectFromApi
                                    )

                                    if (isCurrentProjectFromApi) {
                                        // Jika proyek dari API, update ke API
                                        projectViewModel.updateProjectInApi(project)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Proyek API sedang diperbarui...")
                                        }
                                    } else {
                                        // Jika proyek lokal (atau baru), tambahkan/perbarui ke Room
                                        if (projectId == null || projectId == 0) {
                                            projectViewModel.addLocalProject(project)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Proyek lokal baru berhasil ditambahkan!")
                                            }
                                        } else {
                                            projectViewModel.updateLocalProject(project)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Proyek lokal berhasil diperbarui!")
                                            }
                                        }
                                    }
                                    resetForm()
                                    showForm = false
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Nama proyek tidak boleh kosong!")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = saveChangesButtonColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f)
                                .padding(horizontal = 4.dp) // Sesuaikan padding
                        ) {
                            Text(
                                if (isCurrentProjectFromApi) "Perbarui Proyek API"
                                else if (projectId == null || projectId == 0) "Simpan Proyek Lokal Baru"
                                else "Perbarui Proyek Lokal",
                                color = Color.White
                            )
                        }
                    }

                    // Tambahkan tombol untuk mengupload proyek lokal ke API
                    if (projectId != null && projectId != 0 && !isCurrentProjectFromApi) { // Hanya untuk proyek lokal yang sudah ada
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (projectName.isNotBlank()) {
                                    val projectToUpload = Project(
                                        id = 0, // ID harus 0 untuk POST ke API (API akan menghasilkan ID baru)
                                        projectName = projectName,
                                        description = description,
                                        location = location,
                                        clientName = supervisor,
                                        startDate = startDate,
                                        endDate = endDate,
                                        status = status,
                                        createdBy = "Admin",
                                        isFromApi = false // Ini adalah proyek lokal yang diupload
                                    )
                                    projectViewModel.addProjectToApi(projectToUpload) // Panggil addProjectToApi
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Proyek lokal sedang diunggah ke API...")
                                    }
                                    resetForm()
                                    showForm = false
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Nama proyek tidak boleh kosong untuk diunggah!")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = uploadToApiButtonColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Unggah Proyek ke API", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }


                    if (showStartDatePicker) { // Tidak perlu lagi cek isCurrentProjectFromApi di sini
                        CustomDatePickerDialog(
                            onDismissRequest = { showStartDatePicker = false },
                            onDateSelected = { date ->
                                startDate = date
                                showStartDatePicker = false
                            },
                            initialDate = startDate
                        )
                    }
                    if (showEndDatePicker) { // Tidak perlu lagi cek isCurrentProjectFromApi di sini
                        CustomDatePickerDialog(
                            onDismissRequest = { showEndDatePicker = false },
                            onDateSelected = { date ->
                                endDate = date
                                showEndDatePicker = false
                            },
                            initialDate = endDate
                        )
                    }
                }
            } else {
                Text(
                    text = "Daftar Proyek",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProjectTable(
                    projects = combinedProjects,
                    onEdit = { project ->
                        projectId = project.id
                        projectName = project.projectName
                        description = project.description.orEmpty()
                        location = project.location.orEmpty()
                        supervisor = project.clientName.orEmpty()
                        startDate = project.startDate.orEmpty()
                        endDate = project.endDate.orEmpty()
                        status = project.status.orEmpty()
                        isCurrentProjectFromApi = project.isFromApi // Tetapkan nilai ini
                        showForm = true
                    },
                    onDelete = { project ->
                        coroutineScope.launch {
                            if (project.isFromApi) {
                                projectViewModel.deleteProjectFromApi(project)
                                snackbarHostState.showSnackbar("Proyek API sedang dihapus...")
                            } else {
                                projectViewModel.deleteLocalProject(project)
                                snackbarHostState.showSnackbar("Proyek lokal berhasil dihapus!")
                            }
                            if (projectId == project.id) resetForm()
                        }
                    },
                    editButtonColor = editButtonColor,
                    deleteButtonColor = deleteButtonColor,
                    tableHeaderBackgroundColor = tableHeaderBackgroundColor,
                    statusOngoingBgColor = statusOngoingBgColor,
                    statusOngoingTextColor = statusOngoingTextColor,
                    statusCompleteBgColor = statusCompleteBgColor,
                    statusCompleteTextColor = statusCompleteTextColor
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = {
                            resetForm()
                            showForm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = createProjectButtonColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Proyek", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Buat Proyek Baru", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ... (CustomInputField, StatusDropdown, ProjectTable, TableCell, CustomDatePickerDialog tetap sama)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false, // Tetap ada readOnly, tapi tidak lagi dipaksakan oleh isCurrentProjectFromApi di luar
    singleLine: Boolean = true,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly, // Digunakan jika ingin membuat field tertentu read-only berdasarkan kondisi lain
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color.LightGray),
                RoundedCornerShape(4.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(4.dp),
        trailingIcon = trailingIcon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    statusOptions: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // Tetap ada enabled, tapi tidak lagi dipaksakan oleh isCurrentProjectFromApi
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedStatus,
            onValueChange = {},
            readOnly = true,
            enabled = enabled, // Digunakan jika ingin membuat dropdown read-only berdasarkan kondisi lain
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, Color.LightGray),
                    RoundedCornerShape(4.dp)
                ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(4.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun ProjectTable(
    projects: List<Project>,
    onEdit: (Project) -> Unit,
    onDelete: (Project) -> Unit,
    editButtonColor: Color,
    deleteButtonColor: Color,
    tableHeaderBackgroundColor: Color,
    statusOngoingBgColor: Color,
    statusOngoingTextColor: Color,
    statusCompleteBgColor: Color,
    statusCompleteTextColor: Color
) {
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tableHeaderBackgroundColor)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("No", 40.dp)
            TableHeaderCell("Nama Proyek", 120.dp)
            TableHeaderCell("Sumber", 80.dp)
            TableHeaderCell("Supervisor", 120.dp)
            TableHeaderCell("Lokasi", 100.dp)
            TableHeaderCell("Tgl Mulai", 120.dp)
            TableHeaderCell("Tgl Berakhir", 120.dp)
            TableHeaderCell("Status", 100.dp)
            TableHeaderCell("Aksi", 140.dp)
        }
        if (projects.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tidak ada proyek yang ditemukan.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            projects.forEachIndexed { index, project ->
                val rowBackgroundColor = when {
                    project.isFromApi -> Color(0xFFE0F7FA) // Warna berbeda untuk proyek dari API
                    index % 2 == 0 -> Color.White
                    else -> Color(0xFFFAFAFA)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBackgroundColor)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell("${index + 1}", 40.dp)
                    TableCell(project.projectName, 120.dp)
                    TableCell(if (project.isFromApi) "API" else "Lokal", 80.dp)
                    TableCell(project.clientName.orEmpty().ifBlank { "-" }, 120.dp)
                    TableCell(project.location.orEmpty().ifBlank { "-" }, 100.dp)
                    TableCell(project.startDate.orEmpty().ifBlank { "-" }, 120.dp)
                    TableCell(project.endDate.orEmpty().ifBlank { "-" }, 120.dp)
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(
                                color = when (project.status) {
                                    "Pending" -> statusOngoingBgColor
                                    "Done" -> statusCompleteBgColor
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.LightGray),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.status.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (project.status) {
                                "Pending" -> statusOngoingTextColor
                                "Done" -> statusCompleteTextColor
                                else -> Color.Black
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.width(140.dp).padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onEdit(project) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = editButtonColor // Tombol edit selalu aktif
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            enabled = true // Tombol edit selalu aktif sekarang
                        ) {
                            Text("Edit", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { onDelete(project) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = deleteButtonColor
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            enabled = true // Tombol delete selalu aktif
                        ) {
                            Text("Hapus", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TableCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun CustomDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String
) {
    val calendar = remember { Calendar.getInstance() }
    val context = LocalContext.current

    val initialYear = remember {
        try {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(initialDate)
            date?.let { Calendar.getInstance().apply { time = it }.get(Calendar.YEAR) } ?: calendar.get(Calendar.YEAR)
        } catch (e: Exception) {
            calendar.get(Calendar.YEAR)
        }
    }
    val initialMonth = remember {
        try {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(initialDate)
            date?.let { Calendar.getInstance().apply { time = it }.get(Calendar.MONTH) } ?: calendar.get(Calendar.MONTH)
        } catch (e: Exception) {
            calendar.get(Calendar.MONTH)
        }
    }
    val initialDay = remember {
        try {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(initialDate)
            date?.let { Calendar.getInstance().apply { time = it }.get(Calendar.DAY_OF_MONTH) } ?: calendar.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            calendar.get(Calendar.DAY_OF_MONTH)
        }
    }

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val newCalendar = Calendar.getInstance().apply { set(selectedYear, selectedMonth, selectedDay) }
            onDateSelected(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(newCalendar.time))
        },
        initialYear, initialMonth, initialDay
    )

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose {
            datePickerDialog.dismiss()
        }
    }
}