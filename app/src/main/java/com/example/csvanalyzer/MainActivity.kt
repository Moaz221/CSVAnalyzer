package com.example.csvanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.csvanalyzer.ui.navigation.AppNavigation
import com.example.csvanalyzer.ui.theme.CSVAnalyzerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CSVAnalyzerTheme {
                // كل التنقل بقى من هنا
                AppNavigation()
            }
        }
    }
}