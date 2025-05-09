package com.example.test.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.viewmodel.DataEntryViewModel
import com.example.test.data.DataEntry

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DataEntryScreen(navController: NavHostController, viewModel: DataEntryViewModel = viewModel()) {
    val showForm = remember { mutableStateOf(false) }
    val dataList by viewModel.allEntries.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    Row(Modifier.fillMaxSize()) {
        if (showForm.value) {
            val entryState = remember { mutableStateOf(DataEntry()) }
            DataEntryForm(
                entry = entryState.value,
                onEntryChange = { field, value ->
                    entryState.value = entryState.value.copyField(field, value)
                },
                onSave = {
                    viewModel.insertEntry(entryState.value)
                    showForm.value = false
                },
                onClose = { showForm.value = false },
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
                Button(onClick = { showForm.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                Spacer(Modifier.width(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Search...") },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalScrollableTable(dataList)
        }
    }
}

@Composable
fun HorizontalScrollableTable(data: List<DataEntry>) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        HeaderRow()
        data.forEach { entry ->
            DataRow(entry)
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val headers = listOf("ID", "Description", "Target", "Features", "Start Date", "End Date", "Status", "Action")
        headers.forEach { header ->
            TableCell(header, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DataRow(entry: DataEntry) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val row = listOf(
            entry.id.toString(), entry.problem, entry.target, entry.features,
            entry.startDate, entry.endDate, entry.status
        )
        row.forEach { cell ->
            TableCell(cell)
        }
        TableCell {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionButton("Edit", Color(0xFF4CAF50))
                ActionButton("View", Color(0xFF9E9E9E))
                ActionButton("Delete", Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun TableCell(text: String, fontWeight: FontWeight = FontWeight.Normal) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .padding(8.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = fontWeight)
    }
}

@Composable
fun TableCell(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .padding(8.dp)
    ) {
        content()
    }
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun DataEntryForm(
    entry: DataEntry,
    onEntryChange: (String, String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    navController: NavHostController
) {
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
            "startDate" to "Start Date",
            "endDate" to "End Date",
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