package com.example.test.screen

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.data.DatasetReply
import com.example.test.viewmodel.AllDatasetRepliesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatasetScreen(
    navController: NavHostController,
    viewModel: AllDatasetRepliesViewModel = viewModel(
        factory = AllDatasetRepliesViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val datasetReplies by viewModel.datasetReplies.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daftar Balasan Dataset",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                Text("Memuat data...", color = Color.Gray)
            }
            errorMessage != null -> {
                Text(
                    text = "Terjadi kesalahan: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Button(onClick = { viewModel.fetchAllDatasetReplies() }) {
                    Text("Coba Lagi")
                }
            }
            datasetReplies != null && datasetReplies!!.isNotEmpty() -> {
                val scrollState = rememberScrollState() // Shared scroll state for horizontal scrolling

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0))
                            .padding(vertical = 8.dp)
                            .horizontalScroll(scrollState), // Apply horizontal scroll here
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = "#", weight = 0.5f, header = true)
                        TableCell(text = "Proyek Terkait", weight = 2f, header = true)
                        TableCell(text = "Pesan Balasan", weight = 3f, header = true)
                        TableCell(text = "Link Dataset", weight = 1.5f, header = true)
                        TableCell(text = "Waktu Diterima", weight = 2f, header = true)
                    }

                    // Table Rows
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()) // Vertical scroll for the list
                    ) {
                        datasetReplies!!.forEachIndexed { index, dataset ->
                            DatasetReplyRow(
                                index = index + 1,
                                dataset = dataset,
                                onDownloadClick = { link ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                horizontalScrollState = scrollState // Pass the shared scroll state
                            )
                        }
                    }
                }
            }
            else -> {
                Text(
                    text = "Tidak ada balasan dataset yang tersedia.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    header: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .weight(weight)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (header) 14.sp else 13.sp,
        color = if (header) Color.DarkGray else Color.Black,
        textAlign = textAlign
    )
}

@Composable
fun DatasetReplyRow(
    index: Int,
    dataset: DatasetReply,
    onDownloadClick: (String) -> Unit,
    horizontalScrollState: ScrollState
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .horizontalScroll(horizontalScrollState) // Apply horizontal scroll here
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = index.toString(), weight = 0.5f, textAlign = TextAlign.Center)

        // Project Name (clickable)
        Text(
            text = dataset.projectName ?: "N/A",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = 8.dp)
                .clickable {
                }
        )

        TableCell(text = dataset.messageText ?: "N/A", weight = 3f)

        // Download Button or Link
        Box(
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            dataset.datasetLink?.let { link ->
                if (link.isNotBlank()) {
                    Button(
                        onClick = { onDownloadClick(link) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Unduh", color = Color.White, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "Tidak Ada",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } ?: Text(
                text = "Tidak Ada",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        TableCell(text = formatDate(dataset.createdAt), weight = 2f)
    }
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(parser.parse(dateString)!!)
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}