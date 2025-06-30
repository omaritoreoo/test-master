package com.example.test.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.screen.DashboardMainScreen
import com.example.test.screen.DataEntryScreen
import com.example.test.screen.LandingPage
import com.example.test.screens.LoginScreen
import com.example.test.screens.SignUpScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("landing") {
            LandingPage(navToDashboard = {
                navController.navigate("dashboard_main")
            })
        }
        composable("dashboard_main") { DashboardMainScreen() }
    }
}
