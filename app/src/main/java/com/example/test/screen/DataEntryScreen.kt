package com.example.test.screen

import DataEntryViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.accompanist.flowlayout.FlowRow
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DataEntryScreen(navController: NavHostController, viewModel: DataEntryViewModel = viewModel()) {
    val showForm = remember { mutableStateOf(false) }
    val dataList by viewModel.allEntries.collectAsState(initial = emptyList())
    val entryState = remember { mutableStateOf(DataEntry()) }
    var query by remember { mutableStateOf("") }
    val selectedEntryForView = remember { mutableStateOf<DataEntry?>(null) }

    val filteredList = if (query.isBlank()) dataList else dataList.filter {
        it.problem.contains(query, true) ||
                it.target.contains(query, true) ||
                it.features.contains(query, true) ||
                it.status.contains(query, true)
    }

    Row(Modifier.fillMaxSize()) {
        if (showForm.value) {
            DataEntryForm(
                entry = entryState.value,
                onEntryChange = { field, value -> entryState.value = entryState.value.copyField(field, value) },
                onSave = {
                    if (entryState.value.id == 0) viewModel.insertEntry(entryState.value)
                    else viewModel.updateEntry(entryState.value)
                    entryState.value = DataEntry()
                    showForm.value = false
                },
                onClose = {
                    entryState.value = DataEntry()
                    showForm.value = false
                },
                navController = navController
            )
        }

        Column(Modifier.weight(1f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    entryState.value = DataEntry()
                    showForm.value = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                Spacer(Modifier.width(16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search...") },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalScrollableTable(
                data = filteredList,
                onEdit = {
                    entryState.value = it
                    showForm.value = true
                },
                onDelete = { viewModel.deleteEntry(it) },
                onView = { selectedEntryForView.value = it }
            )

            // Show detail dialog
            selectedEntryForView.value?.let {
                EntryDetailView(entry = it, onClose = { selectedEntryForView.value = null })
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
        data.forEach { entry ->
            DataRow(entry, onEdit, onDelete, onView)
        }
    }
}

@Composable
fun HeaderRow() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        val headers = listOf("ID", "Description", "Target", "Features", "Start Date", "End Date", "Status", "Action")
        headers.forEach { TableCell(it, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun DataRow(entry: DataEntry, onEdit: (DataEntry) -> Unit, onDelete: (DataEntry) -> Unit, onView: (DataEntry) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        listOf(
            entry.id.toString(), entry.problem, entry.target, entry.features,
            entry.startDate, entry.endDate, entry.status
        ).forEach { TableCell(it) }

        TableCell(modifier = Modifier.width(200.dp)) {
            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                ActionButton("View", Color(0xFF2196F3)) { onView(entry) }
                ActionButton("Edit", Color(0xFF4CAF50)) { onEdit(entry) }
                ActionButton("Delete", Color(0xFFF44336)) { onDelete(entry) }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier.width(140.dp)
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = fontWeight)
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
        modifier = Modifier.heightIn(min = 32.dp)
    ) {
        Text(text, fontSize = 12.sp)
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
            Text("Entry Detail", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("ID: ${entry.id}")
                Text("Problem: ${entry.problem}")
                Text("Target: ${entry.target}")
                Text("Features: ${entry.features}")
                Text("Start Date: ${entry.startDate}")
                Text("End Date: ${entry.endDate}")
                Text("Status: ${entry.status}")
            }
        }
    )
}

@Composable
fun DataEntryForm(
    entry: DataEntry,
    onEntryChange: (String, String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    fun showDatePicker(field: String) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormatter.format(calendar.time)
                onEntryChange(field, selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        Modifier
            .width(450.dp)
            .fillMaxHeight()
            .padding(16.dp)
            .background(Color(0xFFFFF0F5))
    ) {
        Text("Data Entry", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val fields = listOf(
            "problem" to "Description Problem",
            "target" to "Target",
            "features" to "Features",
            "status" to "Status"
        )

        fields.forEach { (key, label) ->
            OutlinedTextField(
                value = entry.getField(key),
                onValueChange = { onEntryChange(key, it) },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clickable { showDatePicker("startDate") }
        ) {
            OutlinedTextField(
                value = entry.startDate,
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(4.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clickable { showDatePicker("endDate") }
        ) {
            OutlinedTextField(
                value = entry.endDate,
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Return")
            }
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                Text("Save Changes")
            }
        }
    }
}
