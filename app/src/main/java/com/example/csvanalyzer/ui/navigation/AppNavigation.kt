package com.example.csvanalyzer.ui.navigation

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csvanalyzer.ui.screens.AnalyzingScreen
import com.example.csvanalyzer.ui.screens.HomeScreen
import com.example.csvanalyzer.ui.screens.PdfReportScreen
import com.example.csvanalyzer.ui.screens.ui.ResultScreen
import com.example.csvanalyzer.viewmodel.MainUiState
import com.example.csvanalyzer.viewmodel.MainViewModel
import com.example.csvanalyzer.viewmodel.ResultViewModel
import com.example.csvanalyzer.ui.theme.BgDark

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is MainUiState.Loading -> {
                if (navController.currentDestination?.route != Screen.Analyzing.route) {
                    navController.navigate(Screen.Analyzing.route) {
                        launchSingleTop = true
                    }
                }
            }
            is MainUiState.Success -> {
                navController.navigate(Screen.Result.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
                mainViewModel.resetState()
            }
            is MainUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                mainViewModel.resetState()
                navController.popBackStack(Screen.Home.route, inclusive = false)
            }
            MainUiState.Idle -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(viewModel = mainViewModel, navController = navController)
            }
            composable(route = Screen.Analyzing.route) {
                AnalyzingScreen(
                    onBack = {
                        mainViewModel.resetState()
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
            composable(route = Screen.Result.route) {
                val resultViewModel: ResultViewModel = viewModel()
                LaunchedEffect(mainViewModel.currentRawJson, mainViewModel.currentTitle) {
                    val isDemo = mainViewModel.currentTitle.contains("GoBike", ignoreCase = true)
                    resultViewModel.parseResultJson(
                        json = mainViewModel.currentRawJson,
                        isDemo = isDemo,
                        localProfile = mainViewModel.currentLocalProfile
                    )
                }
                ResultScreen(
                    viewModel = resultViewModel,
                    onBack = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onExportClick = { _, _ ->
                        navController.navigate(Screen.PdfReport.route)
                    }
                )
            }
            composable(route = Screen.PdfReport.route) {
                PdfReportScreen(
                    jsonResult = mainViewModel.currentRawJson,
                    title = mainViewModel.currentTitle,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.History.route) {
                PlaceholderScreen("History", navController)
            }
            composable(route = Screen.Reports.route) {
                PlaceholderScreen("Reports", navController)
            }
            composable(route = Screen.Settings.route) {
                PlaceholderScreen("Settings", navController)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, navController: NavHostController) {
    Scaffold(
        containerColor = BgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title Screen Coming Soon",
                color = com.example.csvanalyzer.ui.theme.TextGray,
                fontSize = 18.sp
            )
        }
    }
}
