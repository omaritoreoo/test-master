package com.example.test.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.data.User // Import your User data class
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisibility by rememberSaveable { mutableStateOf(false) }


    val scope = rememberCoroutineScope() // Still needed for coroutine scope

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF1F8)) // background layar pink muda
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Icon",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    color = Color(0xFFFF7AC2),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card: pink full-width box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD9EC))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "intelligence creations",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Create New Account", fontSize = 14.sp)

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
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisibility)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisibility)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                Icon(imageVector = image, contentDescription = "Toggle confirm password visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                    errorMessage = "Please fill in all fields."
                                } else if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match."
                                } else {
                                    // Using Room: Create a User object and save it via the ViewModel
                                    val newUser = User(email = email, username = username, password = password)
                                    scope.launch {
                                        userViewModel.saveUser(newUser)
                                        errorMessage = "Account created. You can login now."
                                        // Optionally clear fields after successful registration
                                        email = ""
                                        username = ""
                                        password = ""
                                        confirmPassword = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7AC2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Create", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF7AC2))
                        ) {
                            Text("Return", color = Color(0xFFFF7AC2))
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Bagian koneksi sosial dengan background pink
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFD9EC))
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Or Connected with", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Facebook", color = Color(0xFFFF7AC2), fontSize = 12.sp)
                    Text("LinkedIn", color = Color(0xFFFF7AC2), fontSize = 12.sp)
                    Text("Google", color = Color(0xFFFF7AC2), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(130.dp)) // beri ruang untuk maskot
        }

        // Maskot di kanan bawah dengan background putih
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.White)
                .height(130.dp)
                .width(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.maskot),
                contentDescription = "Maskot",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}