package com.example.csvanalyzer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.theme.BgDark
import com.example.csvanalyzer.ui.theme.GoldPremium
import com.example.csvanalyzer.ui.theme.TextGray
import com.example.csvanalyzer.ui.theme.TextWhite

@Composable
fun PdfReportScreen(
    jsonResult: String,
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Export Report",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(title, color = GoldPremium, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "PDF screen is connected to Navigation successfully.\nNext step: generate real PDF.",
            color = TextGray,
            fontSize = 14.sp
        )
    }
}