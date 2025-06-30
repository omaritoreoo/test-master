package com.example.test.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.test.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current

    val loggedInUser by userViewModel.loggedInUser.observeAsState(initial = null)
    val username = loggedInUser?.username
    val emailAddress = loggedInUser?.email

    // --- REMOVED THIS LINE ---
    // LaunchedEffect(username) {
    //     username?.let { profileViewModel.loadProfile(it) }
    // }
    // The ProfileViewModel now receives updates via setLoggedInUsername from DashboardMainScreen

    val profile by profileViewModel.profile.observeAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(profile) { // Only react to profile changes
        profile?.let {
            firstName = it.firstName
            lastName = it.lastName
            bio = it.bio
            title = it.title
            imageUri = it.photoUri?.let(Uri::parse)
        }
    }

    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "profile_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfileApp")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = tempCameraImageUri
            tempCameraImageUri?.let { profileViewModel.updatePhotoUri(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            profileViewModel.updatePhotoUri(uri.toString())
        }
    }

    val requiredPermissions = remember {
        mutableStateListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.all { it.value }
        if (!allGranted) {
            // Handle scenario where permissions are not granted, e.g., show a Toast or SnackBar
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

    // Create a scroll state for the Column
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // Apply verticalScroll here
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFDE5C9D), CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFDE5C9D), CircleShape),
                tint = Color(0xFFDE5C9D)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button Row for photo
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    tempCameraImageUri = createImageUri(context)
                    tempCameraImageUri?.let { takePictureLauncher.launch(it) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE5C9D)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE5C9D)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Upload File")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Form for new Profile fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileTextField("Username:", username ?: "Loading...", readOnly = true) {}
                ProfileTextField("Email Address:", emailAddress ?: "Loading...", readOnly = true) {}

                ProfileTextField("First Name:", firstName) { firstName = it }
                ProfileTextField("Last Name:", lastName) { lastName = it }
                ProfileTextField("Title:", title) { title = it }
                // For bio, consider using maxLines = Int.MAX_VALUE and minLines for multiline input
                ProfileTextField("Bio:", bio, singleLine = false) { bio = it } // Allow multi-line for bio
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                username?.let { usr ->
                    emailAddress?.let { email ->
                        profileViewModel.saveProfile(
                            Profile(
                                username = usr,
                                firstName = firstName,
                                lastName = lastName,
                                emailAddress = email,
                                photoUri = imageUri?.toString(),
                                bio = bio,
                                title = title
                            )
                        )
                        // No need to call loadProfile here; the ProfileViewModel is reactive
                        // to username changes which are handled by DashboardMainScreen.
                        // If you want immediate UI update after save, observe 'profile' LiveData.
                        navController.popBackStack() // Navigate back after saving
                    } ?: run {
                        // Handle case where email is null (e.g., show a SnackBar)
                        Log.e("ProfileScreen", "Email address is null when trying to save profile.")
                    }
                } ?: run {
                    // Handle case where username is null (e.g., navigate back to login or show an error)
                    Log.e("ProfileScreen", "Username is null when trying to save profile.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE5C9D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
        // Add some padding at the bottom so the last elements aren't cut off by the navigation bar or system gestures
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    onChange: (String) -> Unit
) {
    Text(label)
    TextField(
        value = value,
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        singleLine = singleLine,
        readOnly = readOnly
    )
}