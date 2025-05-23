package com.example.test.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.util.UserDataStore

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF1F8)) // Background bawah putih
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Bisa diganti dengan ikon bulat pink jika berbeda
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

        // Bagian atas - Header + Form
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFFADAF1)) // Latar form pink muda
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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.Visibility, contentDescription = "Toggle Password")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {}
                )
                Text("Remember Me", modifier = Modifier.padding(end = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text("Forgot Password?", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (email == UserDataStore.email && password == UserDataStore.password) {
                        navController.navigate("dashboard_main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        errorMessage = "Invalid email or password."
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

        // Bagian bawah - Maskot
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
