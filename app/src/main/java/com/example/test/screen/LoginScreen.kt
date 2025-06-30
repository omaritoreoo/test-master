package com.example.test.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.* // Pastikan ini diimpor
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.viewmodel.UserViewModel
import kotlinx.coroutines.launch // Penting: Tambahkan ini untuk CoroutineScope

@Composable
fun LoginScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    // Hapus baris ini: val userFromDb by userViewModel.getUserByEmail(email).observeAsState(initial = null)
    // Kita tidak lagi mengamati userFromDb di sini, melainkan memanggil fungsi loginUser.

    val scope = rememberCoroutineScope() // Digunakan untuk meluncurkan coroutine

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF1F8))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "login page",
                color = Color(0xFFFF7AC2),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        // Form
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFFADAF1))
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("intelligence creations", fontSize = 22.sp, color = Color.DarkGray)
            Text("Welcome! Please login to your account.", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisibility)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = false, // You might want to implement remember me logic later
                    onCheckedChange = {}
                )
                Text("Remember Me", modifier = Modifier.padding(end = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text("Forgot Password?", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password."
                    } else {
                        // Memanggil fungsi loginUser dari ViewModel
                        scope.launch {
                            val loggedInUser = userViewModel.loginUser(email, password)
                            if (loggedInUser != null) {
                                // Login berhasil
                                errorMessage = "" // Hapus pesan error sebelumnya
                                navController.navigate("landing") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                // Login gagal
                                errorMessage = "Invalid email or password."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7AC2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.navigate("signup") },
                border = BorderStroke(1.dp, Color(0xFFFF7AC2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up", color = Color(0xFFFF7AC2))
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Or login with ")
                Text("Facebook", color = Color(0xFFAD42F4))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LinkedIn", color = Color(0xFFAD42F4))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google", color = Color(0xFFAD42F4))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.maskot),
                contentDescription = "Mascot",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}