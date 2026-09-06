package com.example.csvanalyzer.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Analyzing : Screen("analyzing")
    data object Result : Screen("result")
    data object PdfReport : Screen("pdf_report")
    data object History : Screen("history")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}
