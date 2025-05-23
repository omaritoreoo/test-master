package com.example.test.screen

import android.app.Application
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.test.R
import com.example.test.screens.ProfileScreen
import com.example.test.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(currentTitle: String, onMenuClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp)
                )
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
        "profile" -> "Edit Profil"
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
        // Profile section with clickable modifier
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("profile") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 12.dp)
        ) {
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
        composable("dashboard") { DashboardScreen() }
        composable("data_entry") { backStackEntry ->
            DataEntryScreen(navController)
        }
        composable("train_model") { backStackEntry ->
            TrainModelScreen()
        }
        composable("profile") { backStackEntry ->
            ProfileScreen(navController)
        }
    }
}


@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "Dashboard",
            color = Color(0xFFDC74D8),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            Text("Deteksi Hewan")
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDADA)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Visualisasi Proses Training", fontWeight = FontWeight.Bold)
                Row {
                    Image(
                        painter = painterResource(R.drawable.train),
                        contentDescription = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF8A7)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Perbandingan akurasi antar model", fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("45%", color = Color.Blue, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .width(30.dp)
                                .background(Color.Blue)
                        )
                        Text("CNN")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("65%", color = Color.Red, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .height(80.dp)
                                .width(30.dp)
                                .background(Color.Red)
                        )
                        Text("YOLO")
                    }
                }
            }
        }

        Text("Tabel Data", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            TableContent()
        }
    }
}

@Composable
fun TableContent() {
    val rows = listOf(
        listOf("01", "Intelligence eng...", "JSON", "Prediksi Harga dengan akurasi 90%", "On-going", "View"),
        listOf("02", "Intelligence eng...", "JSON", "Deteksi Jenis Hewan", "Success", "View")
    )

    Column {
        Row(
            modifier = Modifier
                .background(Color(0xFFDCE6FF))
                .padding(8.dp)
        ) {
            listOf("#", "From", "Jenis Data", "Meaningful Objective", "Status", "Actions").forEach {
                Text(it, modifier = Modifier.width(140.dp), fontWeight = FontWeight.Bold)
            }
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .background(Color(0xFFF4F6FF))
                    .padding(8.dp)
            ) {
                row.forEachIndexed { index, item ->
                    val color = when (item) {
                        "On-going" -> Color(0xFFFFB300)
                        "Success" -> Color(0xFF00C853)
                        else -> Color.Black
                    }
                    Text(
                        text = item,
                        modifier = Modifier.width(140.dp),
                        color = if (index == 4) color else Color.Black
                    )
                }
            }
        }
    }
}
