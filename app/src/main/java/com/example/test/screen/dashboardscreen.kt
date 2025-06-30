package com.example.test.screen

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.test.R
import com.example.test.data.Project
import com.example.test.data.MeaningfulObjectives
import com.example.test.data.ProjectWithMeaningfulObjectives
import com.example.test.screens.ProfileScreen // Make sure this import is correct if ProfileScreen is used elsewhere
import com.example.test.viewmodel.ProfileViewModel
import com.example.test.viewmodel.ProjectViewModel
import com.example.test.viewmodel.MeaningfulObjectivesViewModel
import com.example.test.viewmodel.UserViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val projectViewModel: ProjectViewModel = viewModel(
        factory = ProjectViewModel.Factory(application)
    )
    val meaningfulObjectivesViewModel: MeaningfulObjectivesViewModel = viewModel(
        factory = MeaningfulObjectivesViewModel.Factory(application)
    )
    // Instantiate UserViewModel
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(application)
    )
    // Instantiate ProfileViewModel
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(application)
    )

    val loggedInUser by userViewModel.loggedInUser.observeAsState(initial = null)

    val loggedInUsernameLiveData: MutableLiveData<String?> = remember { MutableLiveData() }

    LaunchedEffect(loggedInUser) {
        loggedInUsernameLiveData.value = loggedInUser?.username
    }

    LaunchedEffect(profileViewModel) { // Only run when profileViewModel changes (effectively once)
        profileViewModel.setLoggedInUsername(loggedInUsernameLiveData)
    }


    val localProjects by projectViewModel.allLocalProjects.observeAsState(emptyList())
    val apiProjects by projectViewModel.apiProjects.observeAsState(emptyList())

    val allProjectsWithMeaningfulObjectivesFromApi by meaningfulObjectivesViewModel.apiMeaningfulObjectives.observeAsState(emptyList())

    val combinedMainProjects = remember(localProjects, apiProjects) {
        (localProjects + apiProjects).distinctBy { it.id }.sortedBy { it.projectName }
    }

    val title = when (currentRoute) {
        "data_entry" -> "Problem Framing"
        "train_model" -> "Train Model"
        "profile" -> "Edit Profil"
        "data_api" -> "Data API"
        "project" -> "Projek"
        "request_dataset" -> "Request Dataset"
        "data_processing" -> "Data Processing"
        "dataset" -> "Dataset"
        "documentation" -> "Documentation"
        else -> "Dashboard"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Pass the profileViewModel to the Sidebar
                Sidebar(internalNavController, profileViewModel)
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
                NavigationHost(
                    navController = internalNavController,
                    combinedMainProjects = combinedMainProjects,
                    allProjectsWithMeaningfulObjectives = allProjectsWithMeaningfulObjectivesFromApi,
                    meaningfulObjectivesViewModel = meaningfulObjectivesViewModel
                )
            }
        }
    }
}

@Composable
fun Sidebar(navController: NavHostController, profileViewModel: ProfileViewModel) { // ViewModel is now passed as a parameter
    val profile by profileViewModel.profile.observeAsState()
    val displayedUsername = profile?.username ?: "Admin" // Get username from profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE58DD6))
            .padding(16.dp)
    ) {
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                if (!profile?.photoUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = Uri.parse(profile?.photoUri),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Icon",
                        tint = Color(0xFFE58DD6),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(displayedUsername, color = Color.White, fontWeight = FontWeight.Bold)
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
        DrawerItem(icon = Icons.Default.Work, label = "Project") {
            navController.navigate("project") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.Edit, label = "Problem Framing") {
            navController.navigate("data_entry") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.Work, label = "Request Dataset") {
            navController.navigate("request_dataset") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.Work, label = "Dataset") {
            navController.navigate("dataset") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        DrawerItem(icon = Icons.Default.Work, label = "Data Processing") {
            navController.navigate("data_processing") {
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
        DrawerItem(icon = Icons.Default.Work, label = "Documentation") {
            navController.navigate("documentation") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    combinedMainProjects: List<Project>,
    allProjectsWithMeaningfulObjectives: List<ProjectWithMeaningfulObjectives>,
    meaningfulObjectivesViewModel: MeaningfulObjectivesViewModel
) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                combinedMainProjects,
                allProjectsWithMeaningfulObjectives,
                meaningfulObjectivesViewModel,
                navController
            )
        }
        composable("data_entry") {
            DataEntryScreen(navController)
        }
        composable("train_model") {
            TrainModelScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }

        composable("project") {
            ProjectScreen(navController)
        }
        composable("request_dataset") {
            RequestDatasetScreen(navController)
        }
        composable("dataset") {
            DatasetScreen(navController)
        }
        composable("data_processing") {
            DataProcessingScreen(navController)
        }
        composable("documentation") {
            DocumentationScreen(navController)
        }
    }
}

@Composable
fun DashboardScreen(
    projects: List<Project>,
    allProjectsWithMeaningfulObjectives: List<ProjectWithMeaningfulObjectives>,
    meaningfulObjectivesViewModel: MeaningfulObjectivesViewModel,
    navController: NavHostController
) {
    var showProjectDetailDialog by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }

    var showMODetailDialog by remember { mutableStateOf(false) }
    var selectedProjectWithMeaningfulObjective by remember { mutableStateOf<ProjectWithMeaningfulObjectives?>(null) }

    val selectedProjectMeaningfulObjectives by meaningfulObjectivesViewModel
        .getMeaningfulObjectivesForProjectFromRoom(selectedProject?.id ?: 0)
        .observeAsState(initial = null)


    LaunchedEffect(selectedProject) {
        // Logika tambahan jika diperlukan saat selectedProject berubah, misalnya untuk reset
    }

    // --- IMPORTANT: Logging for project data and status parsing ---
    LaunchedEffect(projects) {
        Log.d("ProjectStatusDebug", "--- Projects Received in DashboardScreen ---")
        Log.d("ProjectStatusDebug", "Total projects: ${projects.size}")
        if (projects.isEmpty()) {
            Log.d("ProjectStatusDebug", "Project list is EMPTY. Check data loading (API/Local).")
        }
        projects.forEachIndexed { index, project ->
            // Use .trim() just in case there are leading/trailing spaces in your actual data
            val statusCleaned = project.status?.trim()
            val statusLowercased = statusCleaned?.lowercase()
            Log.d("ProjectStatusDebug", "$index: Name='${project.projectName}', Raw Status='${project.status}', Cleaned Status='${statusCleaned}', Lowercased Status='${statusLowercased}'")
        }
        Log.d("ProjectStatusDebug", "--- End Projects Log ---")
    }

    // Calculate project status distribution
    val doneProjects = projects.count {
        val statusLowercased = it.status?.trim()?.lowercase() // Use .trim() for robustness
        val isComplete = statusLowercased == "done" // <-- DIUBAH DARI "complete" KE "done"
        if (isComplete) {
            Log.d("ProjectCountMatch", "Matched 'done': ${it.projectName}") // Perbarui log juga
        }
        isComplete
    }
    val pendingProjects = projects.count {
        val statusLowercased = it.status?.trim()?.lowercase() // Use .trim() for robustness
        val isOnGoing = statusLowercased == "pending" // <-- DIUBAH DARI "on going" KE "pending"
        if (isOnGoing) {
            Log.d("ProjectCountMatch", "Matched 'pending': ${it.projectName}") // Perbarui log juga
        }
        isOnGoing
    }

    Log.d("ProjectCountSummary", "Final Done Projects: $doneProjects")
    Log.d("ProjectCountSummary", "Final Pending Projects: $pendingProjects")


    // Determine the maximum count for scaling the bars
    val maxCount = maxOf(doneProjects, pendingProjects, 1) // Ensure it's at least 1 to avoid division by zero


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
        // --- Start of new "Distribusi Status Proyek" Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White), // White background for the chart
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(200.dp) // Set a fixed height for the chart card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Distribusi Status Proyek",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF8BC34A)) // Green for Done
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Selesai", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF2196F3)) // Blue for Pending
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sedang Berjalan", fontSize = 12.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Take remaining vertical space
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Bar for Done Projects
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = doneProjects.toString(), // Display the count
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF8BC34A) // Match bar color: Green
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Small space between count and bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f) // Adjust bar width
                                .height((doneProjects.toFloat() / maxCount * 100).dp) // Scaled height, max 100dp
                                .background(Color(0xFF8BC34A)) // Green for Done bar
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Done", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Space between bars

                    // Bar for Pending Projects
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = pendingProjects.toString(), // Display the count
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2196F3) // Match bar color: Blue
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Small space between count and bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f) // Adjust bar width
                                .height((pendingProjects.toFloat() / maxCount * 100).dp) // Scaled height, max 100dp
                                .background(Color(0xFF2196F3)) // Blue for Pending bar
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pending", fontSize = 14.sp)
                    }
                }
            }
        }
        // --- End of new "Distribusi Status Proyek" Card ---

        Text("Daftar Proyek Terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        Box(modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)) {
            Column {
                Row(modifier = Modifier.background(Color(0xFFF1F1F1)).padding(8.dp)) {
                    TableHeaderCell("#", 40.dp)
                    TableHeaderCell("Nama Proyek", 120.dp)
                    TableHeaderCell("Deskripsi", 120.dp)
                    TableHeaderCell("Status", 80.dp)
                    TableHeaderCell("Supervisor", 100.dp)
                    TableHeaderCell("Tanggal Dibuat", 120.dp)
                    TableHeaderCell("Aksi", 100.dp)
                }

                projects.forEachIndexed { index, project ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {
                        TableCell((index + 1).toString(), 40.dp)
                        TableCell(project.projectName, 120.dp)
                        TableCell(project.description, 120.dp)
                        StatusCell(project.status, 80.dp)
                        TableCell(project.clientName, 100.dp)
                        TableCell(project.startDate, 120.dp)
                        Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    selectedProject = project
                                    showProjectDetailDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("View", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = { navController.navigate("project") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Buat Proyek Baru", color = Color.White)
            }
        }

        Text("Meaningful Objectives", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))

        MeaningfulObjectivesTable(
            projectsWithMeaningfulObjectives = allProjectsWithMeaningfulObjectives,
            onViewClick = { projWithMo ->
                selectedProjectWithMeaningfulObjective = projWithMo
                showMODetailDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showProjectDetailDialog && selectedProject != null) {
        ProjectDetailDialog(
            project = selectedProject!!,
            meaningfulObjectives = selectedProjectMeaningfulObjectives,
            onDismissRequest = {
                showProjectDetailDialog = false
                selectedProject = null
            }
        )
    }

    if (showMODetailDialog && selectedProjectWithMeaningfulObjective != null) {
        MeaningfulObjectiveDetailDialog(
            projectWithMeaningfulObjective = selectedProjectWithMeaningfulObjective!!,
            onDismissRequest = {
                showMODetailDialog = false
                selectedProjectWithMeaningfulObjective = null
            }
        )
    }
}

@Composable
fun TableHeaderCell(text: String, width: Dp) {
    Text(
        text,
        modifier = Modifier
            .width(width)
            .padding(4.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
}

@Composable
fun TableCell(text: String?, width: Dp) {
    Text(
        text ?: "-",
        modifier = Modifier
            .width(width)
            .padding(4.dp),
        fontSize = 14.sp
    )
}

@Composable
fun StatusCell(status: String?, width: Dp) {
    val color = when (status?.lowercase()) {
        "done" -> Color(0xFF4CAF50) // <-- DIUBAH DARI "complete" KE "done"
        "pending" -> Color(0xFF03A9F4) // <-- DIUBAH DARI "on going" KE "pending"
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .width(width)
            .padding(4.dp)
            .background(color = color, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status ?: "-", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun ProjectDetailDialog(project: Project, meaningfulObjectives: MeaningfulObjectives?, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Detail Proyek",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE58DD6),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProjectDetailItem(label = "Nama Proyek", value = project.projectName)
                Spacer(modifier = Modifier.height(8.dp))

                ProjectDetailItem(label = "Deskripsi", value = project.description)
                Spacer(modifier = Modifier.height(8.dp))

                ProjectDetailItem(label = "Lokasi", value = project.location)
                Spacer(modifier = Modifier.height(8.dp))

                ProjectDetailItem(label = "Supervisor", value = project.clientName)
                Spacer(modifier = Modifier.height(8.dp))

                ProjectDetailItem(label = "Start Date", value = project.startDate)
                Spacer(modifier = Modifier.height(8.dp))

                ProjectDetailItem(label = "End Date", value = project.endDate)
                Spacer(modifier = Modifier.height(16.dp))

                meaningfulObjectives?.let { objectives ->
                    Text(
                        text = "Meaningful Objectives",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE58DD6),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    ProjectDetailItem(label = "Organizational", value = objectives.organizational)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "Leading Indicators", value = objectives.leadingIndicators)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "User Outcomes", value = objectives.userOutcomes)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "Model Properties", value = objectives.modelProperties)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE58DD6))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MeaningfulObjectiveDetailDialog(projectWithMeaningfulObjective: ProjectWithMeaningfulObjectives, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Detail Meaningful Objective",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE58DD6),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Akses projectName langsung dari projectWithMeaningfulObjective
                ProjectDetailItem(label = "Project ID", value = projectWithMeaningfulObjective.id.toString())
                Spacer(modifier = Modifier.height(8.dp))
                ProjectDetailItem(label = "Project Name", value = projectWithMeaningfulObjective.projectName) // <-- Akses langsung
                Spacer(modifier = Modifier.height(8.dp))

                // Akses meaningfulObjectives dari projectWithMeaningfulObjective
                projectWithMeaningfulObjective.meaningfulObjectives?.let { objectives ->
                    ProjectDetailItem(label = "Organizational", value = objectives.organizational)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "Leading Indicators", value = objectives.leadingIndicators)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "User Outcomes", value = objectives.userOutcomes)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProjectDetailItem(label = "Model Properties", value = objectives.modelProperties)
                    Spacer(modifier = Modifier.height(16.dp))
                }


                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE58DD6))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProjectDetailItem(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = value ?: "N/A",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// MeaningfulObjectivesTable sekarang menerima List<ProjectWithMeaningfulObjectives>
@Composable
fun MeaningfulObjectivesTable(
    projectsWithMeaningfulObjectives: List<ProjectWithMeaningfulObjectives>, // <-- Ubah parameter
    onViewClick: (ProjectWithMeaningfulObjectives) -> Unit // <-- Ubah tipe parameter callback
) {
    // Filter hanya proyek yang benar-benar memiliki meaningful_objectives
    val meaningfulObjectivesData = projectsWithMeaningfulObjectives.filter { it.meaningfulObjectives != null }


    Box(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 8.dp)) {
        Column {
            Row(modifier = Modifier.background(Color(0xFFF1F1F1)).padding(8.dp)) {
                TableHeaderCell("#", 40.dp)
                TableHeaderCell("Project ID", 100.dp)
                TableHeaderCell("Project Name", 150.dp)
                TableHeaderCell("Organizational", 150.dp)
                TableHeaderCell("Leading Indicators", 150.dp)
                TableHeaderCell("User Outcomes", 150.dp)
                TableHeaderCell("Model Properties", 150.dp)
                TableHeaderCell("Aksi", 100.dp)
            }

            meaningfulObjectivesData.forEachIndexed { index, projWithMo ->
                val mo = projWithMo.meaningfulObjectives // Dapatkan objek MO bersarangnya

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    TableCell((index + 1).toString(), 40.dp)
                    TableCell(projWithMo.id.toString(), 100.dp) // ID Proyek
                    TableCell(projWithMo.projectName, 150.dp) // Nama Proyek langsung
                    TableCell(mo?.organizational, 150.dp) // Akses dari objek bersarang
                    TableCell(mo?.leadingIndicators, 150.dp)
                    TableCell(mo?.userOutcomes, 150.dp)
                    TableCell(mo?.modelProperties, 150.dp)
                    Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { onViewClick(projWithMo) }, // Teruskan ProjectWithMeaningfulObjectives
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("View", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}