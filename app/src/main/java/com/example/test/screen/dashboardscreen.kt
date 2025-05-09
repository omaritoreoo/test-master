package com.example.test.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(currentTitle: String, onMenuClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFFE58DD6))
                Spacer(modifier = Modifier.width(8.dp))
                Text(currentTitle, color = Color(0xFFE58DD6))
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFFE58DD6))
            }
        }
    )
}

@Composable
fun DashboardMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val title = when (currentRoute) {
        "data_entry" -> "Data Entry"
        "train_model" -> "Train Model"
        else -> "Dashboard"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Sidebar(internalNavController)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(currentTitle = title) {
                    coroutineScope.launch { drawerState.open() }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavigationHost(internalNavController)
            }
        }
    }
}

@Composable
fun Sidebar(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE58DD6))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = "Admin", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Admin", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Edit Profile", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem(icon = Icons.Default.Dashboard, label = "Dashboard") {
            navController.navigate("dashboard") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.Edit, label = "Data Entry") {
            navController.navigate("data_entry") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.AutoGraph, label = "Train Model") {
            navController.navigate("train_model") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White)
    }
}

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardContent() }
        composable("data_entry") { DataEntryScreen(navController) }
        composable("train_model") { TrainModelScreen() } // pastikan import tersedia
    }
}

@Composable
fun DashboardContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Visualisasi Proses Training
        DashboardSection(
            title = "Visualisasi Proses Training",
            color = Color(0xFFFFCDD2) // merah muda
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DummyChart("Loss")
                DummyChart("Accuracy")
            }
        }

        // Perbandingan Model
        DashboardSection(
            title = "Status Training",
            color = Color(0xFFFFFFB3) // kuning
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModelBar("Success", 45, Color(0xFF81C784))
                ModelBar("On-going", 100, Color(0xFFFFB74D))
                ModelBar("Failed", 65, Color(0xFFE57373))
            }
        }

        // Tabel Kosong
        DashboardSection(
            title = "Data Training",
            color = Color(0xFFBBDEFB) // biru muda
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("No data available", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun DashboardSection(title: String, color: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun DummyChart(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 150.dp, height = 100.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label)
    }
}

@Composable
fun ModelBar(label: String, percentage: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height((percentage * 1.5).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("$percentage%", fontSize = 12.sp)
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun ColoredBox(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
    )
}
