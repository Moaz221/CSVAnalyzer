package com.example.csvanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PieColors = listOf(
    Color(0xFFEAA93A), Color(0xFF4CAF50), Color(0xFF2196F3),
    Color(0xFFEF5350), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFFF9800), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFFE91E63)
)

@Composable
fun EnhancedPieWrapper(data: Map<String, Int>, columnName: String) {
    val total = data.values.sum().coerceAtLeast(1)
    val sortedData = data.entries.sortedByDescending { it.value }.take(10)

    Column(
        Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            PieChartCompose(data)
        }

        Spacer(Modifier.height(16.dp))

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(0.03f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedData.forEachIndexed { i, entry ->
                val pct = (entry.value.toDouble() / total * 100)
                val color = PieColors[i % PieColors.size]
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(0.2f), CircleShape)
                    )
                    Text(
                        entry.key,
                        color = Color(0xFFF8F8F8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        "${String.format("%.1f", pct)}%",
                        color = Color(0xFFEAA93A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
