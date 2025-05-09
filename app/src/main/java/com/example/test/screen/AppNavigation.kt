package com.example.test.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.screen.ChildEntryScreen
import com.example.test.screen.DashboardMainScreen
import com.example.test.screens.SignUpScreen
import com.example.test.screens.LoginScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("dashboard_main") { DashboardMainScreen() }

        // For ChildEntryScreen, you need to pass the parentId, onAddChild, onDeleteChild, and childDataList.
        composable("data_child/{parentId}") { backStackEntry ->
            val parentId = backStackEntry.arguments?.getString("parentId") ?: ""

            // Dummy data for demonstration, replace with actual logic for childDataList and methods
            val childDataList = listOf<List<String>>() // Replace with actual child data
            val onAddChild: (List<String>) -> Unit = { /* Add logic for adding child */ }
            val onDeleteChild: (String) -> Unit = { /* Add logic for deleting child */ }

            ChildEntryScreen(
                navController = navController,
                parentId = parentId,
                onAddChild = onAddChild,
                onDeleteChild = onDeleteChild,
                childDataList = childDataList
            )
        }
    }
}
