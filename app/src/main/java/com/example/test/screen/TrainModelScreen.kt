package com.example.test.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrainModelScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Train Model",
            fontSize = 20.sp,
            color = Color(0xFFE58DD6),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        StatusModelSection()
        Spacer(modifier = Modifier.height(16.dp))
        ModelPerformanceTable()
    }
}

@Composable
fun StatusModelSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFB3)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Status Model", color = Color.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Group 1: Prediksi Harga Rumah
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModelBar("Decision Tree", 90, Color(0xFFFFA726))
                        ModelBar("Regresi Linear", null, Color(0xFFFFA726))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Prediksi Harga\nRumah",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Spacer antar group
                Spacer(modifier = Modifier.width(32.dp))

                // Group 2: Deteksi Hewan
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModelBar("CNN", 86, Color(0xFFE57373))
                        ModelBar("YOLO", 70, Color(0xFFE57373))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Deteksi Hewan",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ModelBar(modelName: String, accuracy: Int?, barColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .height(130.dp)
            .width(60.dp)
    ) {
        if (accuracy != null) {
            Text(
                "$accuracy%",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .height((accuracy).dp)
                    .width(30.dp)
                    .background(barColor)
            )
        } else {
            Text(
                "on-going",
                fontSize = 10.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(30.dp)
                    .background(barColor, shape = MaterialTheme.shapes.small)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modelName,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ModelPerformanceTable() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("#", modifier = Modifier.width(30.dp))
                Text("Problem", modifier = Modifier.width(150.dp))
                Text("Model Cerdas", modifier = Modifier.width(120.dp))
                Text("Akurasi", modifier = Modifier.width(80.dp))
                Text("Status", modifier = Modifier.width(100.dp))
                Text("Actions", modifier = Modifier.width(160.dp))
            }
            Divider(color = Color.Gray)
            TableRow("01", "Prediksi Harga Rumah", "Regresi Linear", "-", "On-going", Color(0xFFFFA726))
            TableRow("02", "Deteksi Hewan", "CNN", "86%", "Success", Color(0xFF81C784))
        }
    }
}

@Composable
fun TableRow(
    index: String,
    problem: String,
    model: String,
    accuracy: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(index, modifier = Modifier.width(30.dp))
        Text(problem, modifier = Modifier.width(150.dp))
        Text(model, modifier = Modifier.width(120.dp))
        Text(accuracy, modifier = Modifier.width(80.dp))
        Text(status, color = statusColor, modifier = Modifier.width(100.dp))
        Row(modifier = Modifier.width(160.dp)) {
            Button(
                onClick = { /* TODO: Edit logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB)),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Edit", fontSize = 12.sp)
            }
            Button(
                onClick = { /* TODO: Delete logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                Text("Delete", fontSize = 12.sp)
            }
        }
    }
}
