// File: app/src/main/test/screen/DataEntryScreen.kt
package com.example.test.screen

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.data.DataEntry
import com.example.test.data.Project // Tetap import Project karena digunakan untuk projectList
import com.example.test.viewmodel.DataEntryViewModel
import com.example.test.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DataEntryScreen(
    navController: NavHostController,
    viewModel: DataEntryViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel()
) {
    // Menggunakan observeAsState karena allEntries di ViewModel adalah LiveData
    val allEntries by viewModel.allEntries.observeAsState(initial = emptyList())
    val localProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())

    // Gabungkan dan urutkan daftar proyek untuk dropdown di form
    val projectList = remember(localProjects, apiProjects) {
        (localProjects + apiProjects).distinctBy { it.id }.sortedBy { it.projectName.orEmpty() }
    }

    val projectNames = remember(projectList) {
        projectList.map { it.projectName.orEmpty() }
    }

    // `selectedProject` tidak lagi digunakan untuk mengontrol tampilan utama
    // tetapi bisa digunakan untuk menyimpan proyek yang sedang diedit/dilihat jika diperlukan
    // Untuk tujuan ini, saya akan menghapus penggunaannya untuk kontrol tampilan utama.

    // Inisialisasi DataEntry dengan nilai default yang aman dan nullable yang sesuai
    val entryState = remember { mutableStateOf(DataEntry(
        projectName = null,
        projectId = null,
        problemDescription = null,
        target = null,
        stock = null,
        inflow = null,
        outflow = null,
        dataNeeded = null,
        framedBy = null,
        framedById = null,
        dateCreated = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
        createdAt = null,
        updatedAt = null
    )) }
    val showForm = remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val showDetail = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val selectedEntry = remember { mutableStateOf<DataEntry?>(null) } // Digunakan untuk detail/delete

    val verticalScrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScrollState)
    ) {
        // Langsung tampilkan daftar problem framing atau form
        if (showForm.value) {
            DataEntryForm(
                entry = entryState.value,
                onEntryChange = { field, value ->
                    // Ketika projectName berubah, coba cari projectId yang sesuai dari projectList
                    if (field == "projectName") {
                        val project = projectList.find { it.projectName == value }
                        entryState.value = entryState.value.copy(
                            projectName = value,
                            projectId = project?.id // Set projectId berdasarkan nama proyek yang dipilih
                        )
                    } else {
                        entryState.value = entryState.value.copyField(field, value)
                    }
                },
                onSave = {
                    // Pastikan framedById tetap ada saat menyimpan
                    val entryToSave = entryState.value.copy(
                        framedById = entryState.value.framedById ?: 1 // Gunakan yang sudah ada atau default ke 1
                    )

                    if (entryToSave.id == 0) {
                        viewModel.insertEntry(entryToSave)
                    } else {
                        viewModel.updateEntry(entryToSave)
                    }
                    showForm.value = false
                    // Reset entryState untuk form kosong berikutnya
                    entryState.value = DataEntry(
                        projectName = null, // Kembali ke null untuk form baru
                        projectId = null,   // Kembali ke null untuk form baru
                        problemDescription = null,
                        target = null,
                        stock = null,
                        inflow = null,
                        outflow = null,
                        dataNeeded = null,
                        framedBy = "Default User", // Sesuaikan
                        framedById = 1, // Sesuaikan
                        dateCreated = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        createdAt = null,
                        updatedAt = null
                    )
                },
                onClose = { showForm.value = false },
                projectList = projectNames
            )
        } else {
            // Jika form tidak ditampilkan, tampilkan daftar problem framing dan tombol tambah
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daftar Problem Framing",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        // Inisialisasi DataEntry baru untuk form kosong
                        entryState.value = DataEntry(
                            projectName = null, // Mulai dengan proyek null, akan dipilih di form
                            projectId = null,
                            problemDescription = null,
                            target = null,
                            stock = null,
                            inflow = null,
                            outflow = null,
                            dataNeeded = null,
                            framedBy = "Default User", // Sesuaikan
                            framedById = 1, // Sesuaikan
                            dateCreated = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            createdAt = null,
                            updatedAt = null
                        )
                        showForm.value = true
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
                placeholder = { Text("Searching...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                modifier = Modifier.fillMaxWidth(), // Menggunakan fillMaxWidth()
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Filter semua entri berdasarkan query saja
            val filteredEntries = allEntries.filter {
                query.isBlank() ||
                        (it.projectName?.contains(query, true) == true) ||
                        (it.problemDescription?.contains(query, true) == true) ||
                        (it.target?.contains(query, true) == true) ||
                        (it.dataNeeded?.contains(query, true) == true)
            }
            HorizontalScrollableTable(
                data = filteredEntries,
                onEdit = { entryToEdit ->
                    entryState.value = entryToEdit // Set entryState dengan data yang akan diedit
                    showForm.value = true
                },
                onDelete = {
                    selectedEntry.value = it
                    showDeleteConfirmation.value = true
                },
                onView = {
                    selectedEntry.value = it
                    showDetail.value = true
                }
            )

            if (showDetail.value && selectedEntry.value != null) {
                EntryDetailView(entry = selectedEntry.value!!, onClose = { showDetail.value = false })
            }
            if (showDeleteConfirmation.value && selectedEntry.value != null) {
                DeleteConfirmationDialog(
                    entry = selectedEntry.value!!,
                    onDeleteConfirm = {
                        viewModel.deleteEntry(selectedEntry.value!!)
                        showDeleteConfirmation.value = false
                    },
                    onDismiss = { showDeleteConfirmation.value = false }
                )
            }
        }
    }
}

// ProjectTable telah dihapus sepenuhnya karena tidak lagi digunakan.
/*
@Composable
fun ProjectTable(...) { ... }
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryForm(
    entry: DataEntry,
    onEntryChange: (String, String?) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    projectList: List<String>
) {
    val context = LocalContext.current
    var selectedProjectName by remember(entry.projectName) { mutableStateOf(entry.projectName) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .align(Alignment.TopCenter)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Problem Framing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0006D),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(color = Color(0xFFB0006D), thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            ProjectDropdownField(
                selectedProject = selectedProjectName.orEmpty(),
                onProjectSelected = { newName ->
                    selectedProjectName = newName
                    onEntryChange("projectName", newName) // Memicu update projectId di DataEntryScreen
                },
                projectList = projectList
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Deskripsi Masalah", entry.problemDescription) {
                onEntryChange("problemDescription", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Target/Tujuan", entry.target) {
                onEntryChange("target", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Stock", entry.stock) {
                onEntryChange("stock", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Inflow", entry.inflow) {
                onEntryChange("inflow", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Outflow", entry.outflow) {
                onEntryChange("outflow", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Data Diperlukan", entry.dataNeeded) {
                onEntryChange("dataNeeded", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabelAndField("Diframe Oleh", entry.framedBy) {
                onEntryChange("framedBy", it)
            }
            Spacer(modifier = Modifier.height(12.dp))

            val datePickerDialog = remember {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                        onEntryChange("dateCreated", formattedDate)
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                )
            }

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Tanggal Dibuat", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(
                    value = entry.dateCreated.orEmpty(),
                    onValueChange = { onEntryChange("dateCreated", it) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    }
                )
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
                    Text("Save Changes", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HorizontalScrollableTable(
    data: List<DataEntry>,
    onEdit: (DataEntry) -> Unit,
    onDelete: (DataEntry) -> Unit,
    onView: (DataEntry) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        HeaderRow()
        data.forEachIndexed { index, entry ->
            DataRow(entry, index, onEdit, onDelete, onView)
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
    ) {
        val headers = listOf(
            "#",
            "Nama Projek",
            "Deskripsi Masalah",
            "Target",
            "Stock",
            "Inflow",
            "Diframe Oleh",
        )
        headers.forEachIndexed { index, header ->
            val modifier = when (header) {
                "#" -> Modifier.width(60.dp)
                "Nama Projek" -> Modifier.width(150.dp)
                "Deskripsi Masalah" -> Modifier.width(200.dp)
                "Target" -> Modifier.width(150.dp)
                "Stock" -> Modifier.width(100.dp)
                "Inflow" -> Modifier.width(100.dp)
                "Diframe Oleh" -> Modifier.width(150.dp)
                else -> Modifier.width(140.dp)
            }
            TableCell(header, fontWeight = FontWeight.Bold, modifier = modifier)
        }
        TableCell("Action", fontWeight = FontWeight.Bold, modifier = Modifier.width(220.dp))
    }
}

@Composable
fun DataRow(
    entry: DataEntry,
    index: Int,
    onEdit: (DataEntry) -> Unit,
    onDelete: (DataEntry) -> Unit,
    onView: (DataEntry) -> Unit
) {
    val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFF9F9F9)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        listOf(
            "${index + 1}",
            entry.projectName.orEmpty(),
            entry.problemDescription.orEmpty(),
            entry.target.orEmpty(),
            entry.stock.orEmpty(),
            entry.inflow.orEmpty(),
            entry.framedBy.orEmpty(),
        ).forEachIndexed { i, text ->
            val modifier = when (i) {
                0 -> Modifier.width(60.dp)
                1 -> Modifier.width(150.dp)
                2 -> Modifier.width(200.dp)
                3 -> Modifier.width(150.dp)
                4 -> Modifier.width(100.dp)
                5 -> Modifier.width(100.dp)
                6 -> Modifier.width(150.dp)
                else -> Modifier.width(140.dp)
            }
            TableCell(text, modifier = modifier)
        }

        TableCell(modifier = Modifier.width(220.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton("View", Color(0xFF88C28C)) { onView(entry) }
                ActionButton("Edit", Color(0xFF88C28C)) { onEdit(entry) }
                ActionButton("Delete", Color(0xFFE38787)) { onDelete(entry) }
            }
        }
    }
}

@Composable
fun EntryDetailView(entry: DataEntry, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        },
        title = {
            Text(
                text = "Detail Problem Framing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0006D),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LabelValueWithGrayBackground("Nama Proyek", entry.projectName.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Deskripsi Masalah", entry.problemDescription.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Target/Tujuan", entry.target.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Stock", entry.stock.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Inflow", entry.inflow.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Outflow", entry.outflow.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Data Diperlukan", entry.dataNeeded.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Diframe Oleh", entry.framedBy.orEmpty())
                Spacer(modifier = Modifier.height(8.dp))

                LabelValueWithGrayBackground("Tanggal Dibuat", entry.dateCreated.orEmpty())
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}



@Composable
fun LabelAndField(label: String, value: String?, onValueChange: (String?) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = { onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (value.isNullOrEmpty()) Text("") }
        )
    }
}


@Composable
fun DeleteConfirmationDialog(
    entry: DataEntry,
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
                    text = entry.projectName.orEmpty(),
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
