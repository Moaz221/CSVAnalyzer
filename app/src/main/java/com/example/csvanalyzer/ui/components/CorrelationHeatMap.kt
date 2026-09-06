package com.example.csvanalyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun CorrelationHeatMapCompose(correlation: Map<String, Map<String, Double>>) {
    val whiteC = Color(0xFFF8F8F8)
    val grayC = Color(0xFF9BA1A6)
    val textMeasurer = rememberTextMeasurer()

    val cols = correlation.keys.toList()
    
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Correlation Matrix", color = whiteC, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-1", color = grayC, fontSize = 9.sp)
                Canvas(Modifier.width(60.dp).height(10.dp)) {
                    for (i in 0..30) {
                        val v = -1f + (i / 15f)
                        drawRect(
                            color = correlationColor(v),
                            topLeft = Offset(size.width * i / 30f, 0f),
                            size = Size(size.width / 30f + 1f, size.height)
                        )
                    }
                }
                Text("+1", color = grayC, fontSize = 9.sp)
            }
        }

        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val width = size.width
            val height = size.height
            val labelSpace = 80f
            val gridW = width - labelSpace
            val gridH = height - labelSpace
            if (cols.isEmpty()) return@Canvas
            val cellW = gridW / cols.size
            val cellH = gridH / cols.size

            cols.forEachIndexed { i, col1 ->
                cols.forEachIndexed { j, col2 ->
                    val value = correlation[col1]?.get(col2)?.toFloat() ?: 0f
                    val x = labelSpace + j * cellW
                    val y = labelSpace + i * cellH

                    drawRoundRect(
                        color = correlationColor(value),
                        topLeft = Offset(x + 2f, y + 2f),
                        size = Size(cellW - 4f, cellH - 4f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    val txt = String.format("%.2f", value)
                    val layout = textMeasurer.measure(
                        text = txt,
                        style = TextStyle(
                            color = if (abs(value) > 0.5f) Color.White else Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            x + cellW / 2f - layout.size.width / 2f,
                            y + cellH / 2f - layout.size.height / 2f
                        )
                    )
                }
            }

            cols.forEachIndexed { i, col ->
                val layout = textMeasurer.measure(
                    text = col.take(10),
                    style = TextStyle(color = grayC, fontSize = 9.sp)
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        labelSpace - layout.size.width - 6f,
                        labelSpace + i * cellH + cellH / 2f - layout.size.height / 2f
                    )
                )
            }

            cols.forEachIndexed { j, col ->
                val layout = textMeasurer.measure(
                    text = col.take(8),
                    style = TextStyle(color = grayC, fontSize = 9.sp)
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        labelSpace + j * cellW + cellW / 2f - layout.size.width / 2f,
                        labelSpace - layout.size.height - 6f
                    )
                )
            }
        }
    }
}

private fun correlationColor(v: Float): Color {
    return when {
        v > 0f -> {
            val intensity = v.coerceIn(0f, 1f)
            Color(
                red = 0.9f,
                green = 0.6f - intensity * 0.5f,
                blue = 0.2f - intensity * 0.15f,
                alpha = 0.3f + intensity * 0.7f
            )
        }
        v < 0f -> {
            val intensity = (-v).coerceIn(0f, 1f)
            Color(
                red = 0.2f,
                green = 0.4f,
                blue = 0.8f,
                alpha = 0.3f + intensity * 0.7f
            )
        }
        else -> Color.White.copy(alpha = 0.15f)
    }
}
