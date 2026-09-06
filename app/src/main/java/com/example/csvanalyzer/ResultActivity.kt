package com.example.csvanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.csvanalyzer.ui.screens.ui.ResultScreen
import com.example.csvanalyzer.ui.theme.CSVAnalyzerTheme
import com.example.csvanalyzer.viewmodel.ResultViewModel

class ResultActivity : ComponentActivity() {
    private val resultViewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jsonResult = intent.getStringExtra("EXTRA_RESULT_JSON")
        val isDemo = intent.getBooleanExtra("EXTRA_IS_DEMO", false)

        // إرسال البيانات للـ ViewModel عشان يحللها
        resultViewModel.parseResultJson(jsonResult, isDemo)

        setContent {
            CSVAnalyzerTheme {
                ResultScreen(
                    viewModel = resultViewModel,
                    onBack = { finish() },
                    onExportClick = { json, title ->
                        // navigate to PDF activity if needed, or just print
                    }
                )
            }
        }
    }
}