package com.example.test.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.test.data.Profile
import com.example.test.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val profile by viewModel.profile.observeAsState()

    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Load profile data ke form
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            dob = it.dateOfBirth
            region = it.region
            country = it.country
            phone = it.mobile
            imageUri = it.photoUri?.let(Uri::parse)
        }
    }

    // Fungsi buat bikin URI kosong tempat menyimpan gambar dari kamera
    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "profile_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfileApp")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk ambil foto kamera
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = tempCameraImageUri
        }
    }

    // Launcher untuk pilih file dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    // Runtime permission
    val requiredPermissions = remember {
        mutableStateListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT < 33) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
        // Jika semua permission granted, lanjut
        val allGranted = results.all { it.value }
        if (!allGranted) {
            // Handle jika ada yang tidak granted
            // Bisa tampilkan dialog jika ingin
        }
    }

    LaunchedEffect(Unit) {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionsLauncher.launch(notGranted.toTypedArray())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Edit Profil",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFDE5C9D)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(onClick = {
                tempCameraImageUri = createImageUri(context)
                tempCameraImageUri?.let { takePictureLauncher.launch(it) }
            }) {
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Text("Upload File")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Region") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Mobile") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewModel.deleteProfile()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA4D58))
            ) {
                Text("Delete")
            }

            Button(
                onClick = {
                    viewModel.saveProfile(
                        Profile(
                            name = name,
                            dateOfBirth = dob,
                            region = region,
                            country = country,
                            mobile = phone,
                            photoUri = imageUri?.toString()
                        )
                    )
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Save Changes")
            }
        }
    }
}
