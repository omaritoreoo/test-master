package com.example.test.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Rect // Mungkin tidak langsung digunakan, tapi baik untuk ada jika perlu mengukur teks
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Import semua data class yang relevan
import com.example.test.data.DataEntry
import com.example.test.data.DataProcessing
import com.example.test.data.ModelTraining
import com.example.test.data.Project

class PdfGenerator(private val context: Context) {

    /**
     * Menggenerasi dokumen PDF dari detail proyek yang diberikan.
     * File PDF akan disimpan di direktori cache aplikasi (internal storage)
     * agar dapat dibagikan dengan aman menggunakan FileProvider.
     *
     * @param project Objek Project yang akan dicetak.
     * @param dataEntry Objek DataEntry terkait, bisa null.
     * @param dataProcessing Objek DataProcessing terkait, bisa null.
     * @param modelTraining Objek ModelTraining terkait, bisa null.
     * @return File PDF yang berhasil dibuat, atau null jika terjadi kesalahan.
     */
    fun generateProjectPdf(
        project: Project,
        dataEntry: DataEntry?,
        dataProcessing: DataProcessing?,
        modelTraining: ModelTraining?
    ): File? {
        val document = PdfDocument()

        // Definisikan ukuran halaman (misal: A4 Portrait, 595x842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint() // Objek Paint untuk mengatur gaya teks/gambar

        // --- Konten PDF ---
        var yPos = 40f // Posisi Y awal untuk menggambar teks
        val xMargin = 50f // Margin kiri
        val lineHeight = 20f // Tinggi baris default
        val sectionSpacing = 30f // Spasi antar bagian

        // Judul Utama
        paint.textSize = 28f
        paint.isFakeBoldText = true // Membuat teks tebal
        canvas.drawText("Project Report: ${project.projectName}", xMargin, yPos, paint)
        yPos += 40f // Spasi setelah judul

        // Reset gaya teks untuk konten
        paint.textSize = 12f
        paint.isFakeBoldText = false

        // Informasi Umum Proyek (dari Project entity)
        canvas.drawText("Description: ${project.description ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("Status: ${project.status ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("Created By: ${project.createdBy ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("Start Date: ${project.startDate ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("End Date: ${project.endDate ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("Client Name: ${project.clientName ?: "N/A"}", xMargin, yPos, paint)
        yPos += lineHeight
        canvas.drawText("Location: ${project.location ?: "N/A"}", xMargin, yPos, paint)
        yPos += sectionSpacing // Spasi antar bagian

        // Data Entry / Problem Framing
        dataEntry?.let {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("1. Problem Framing", xMargin, yPos, paint)
            yPos += 25f

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Problem Description: ${it.problemDescription}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Target/Objective: ${it.target}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Stock: ${it.stock}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Inflow: ${it.inflow}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Outflow: ${it.outflow}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Data Needed: ${it.dataNeeded}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Framed By: ${it.framedBy}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Date Created (Data Entry): ${it.dateCreated}", xMargin, yPos, paint)
            yPos += sectionSpacing
        }

        // Data Processing
        dataProcessing?.let {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("2. Data Processing", xMargin, yPos, paint)
            yPos += 25f

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Source Data: ${it.dataSourceDescription ?: "N/A"}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Transformation Steps: ${it.processingStepsSummary ?: "N/A"}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Feature Engineering: ${it.featureEngineeringDetails ?: "N/A"}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Processed File Location: ${it.processedDataLocation ?: "N/A"}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Processed File: ${it.processedFile ?: "N/A"}", xMargin, yPos, paint) // Perhatikan ini sekarang 'processedFile'
            yPos += lineHeight
            canvas.drawText("Processing Status: ${it.processingStatus ?: "N/A"}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Created At (Data Processing): ${it.createdAt ?: "N/A"}", xMargin, yPos, paint)
            yPos += sectionSpacing
        }

        // Model Training
        modelTraining?.let {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("3. Model Training", xMargin, yPos, paint)
            yPos += 25f

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Model Name: ${it.modelName}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Model Type: ${it.modelType}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Algorithm: ${it.algorithm}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Training Data: ${it.trainingData}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Performance: ${it.performance}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Model Path: ${it.modelPath}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Refinement Strategy: ${it.refinementStrategy}", xMargin, yPos, paint)
            yPos += lineHeight
            canvas.drawText("Performance After Refinement: ${it.performanceAfterRefinement}", xMargin, yPos, paint)
            yPos += sectionSpacing
        }

        document.finishPage(page)

        // --- Penyimpanan File PDF ---
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanProjectName = project.projectName.replace("[^a-zA-Z0-9.-]".toRegex(), "_") // Membersihkan nama file
        val fileName = "Project_${cleanProjectName}_$timeStamp.pdf"

        // Direktori penyimpanan: Internal cache dir di subfolder "pdf"
        val cacheDir = File(context.cacheDir, "pdf")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs() // Buat folder jika belum ada
        }

        val file = File(cacheDir, fileName)

        return try {
            FileOutputStream(file).use { outputStream -> // Menggunakan use untuk auto-close
                document.writeTo(outputStream)
            }
            file // Mengembalikan File yang sudah dibuat
        } catch (e: IOException) {
            e.printStackTrace()
            null // Mengembalikan null jika ada error I/O
        } finally {
            document.close() // Pastikan dokumen PDF selalu ditutup
        }
    }
}