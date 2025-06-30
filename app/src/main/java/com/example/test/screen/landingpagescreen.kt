package com.example.test.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R // Pastikan ini mengarah ke file R yang benar untuk drawable logo Anda

@Composable
fun LandingPage(navToDashboard: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FC)) // Warna latar belakang seperti di screenshot
            .padding(16.dp)
    ) {
        // Logo di pojok kanan atas
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Kartu Konten Utama
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    // Teks "Selamat Datang di" dan "Intelligence Creations" tetap di tengah
                    Text(
                        text = "Selamat Datang di",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Intelligence Creations",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // --- Bagian yang diganti/ditambahkan: Proses 4 Langkah ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 28.dp)
                    ) {
                        ProcessStep( //
                            number = 1, //
                            title = "Inisiasi Proyek", //
                            description = "Definisikan proyek dan tujuan AI Anda, terintegrasi dengan rekayasa kecerdasan." //
                        )
                        ProcessStep( //
                            number = 2, //
                            title = "Pembingkaian Masalah", //
                            description = "Ubah ide besar menjadi masalah yang terdefinisi jelas dan terukur untuk AI." //
                        )
                        ProcessStep( //
                            number = 3, //
                            title = "Manajemen Data", //
                            description = "Kelola permintaan dataset, proses data, hingga model AI siap dilatih." //
                        )
                        ProcessStep( //
                            number = 4, //
                            title = "Pelatihan & Dokumentasi", //
                            description = "Catat proses pelatihan model dan dokumen penting di setiap tahapan." //
                        )
                    }
                    // --- Akhir Bagian yang diganti/ditambahkan ---

                    Button(
                        onClick = navToDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE290D2)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(50))
                    ) {
                        Text(
                            text = "Masuk ke Dashboard",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable untuk menampilkan satu langkah proses dengan nomor, judul, dan deskripsi.
 */
@Composable
fun ProcessStep(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Kotak nomor
        Box(
            modifier = Modifier
                .size(28.dp) // Ukuran kotak nomor
                .background(Color(0xFFE290D2), RoundedCornerShape(8.dp)), // Warna dan bentuk kotak nomor
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp)) // Spasi antara nomor dan teks
        Column(modifier = Modifier.weight(1f)) { // Kolom untuk judul dan deskripsi
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp // Sesuaikan tinggi baris untuk keterbacaan
            )
        }
    }
}