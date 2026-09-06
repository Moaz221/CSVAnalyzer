package com.example.csvanalyzer.ui.components

import android.graphics.Color
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import kotlin.math.abs

private const val GOLD_COLOR = "#EAA93A"
private const val GREEN_COLOR = "#4CAF50"
private const val GRID_COLOR = "#33FFFFFF"

@Composable
fun BubbleChartCompose(
    dataPoints: List<Triple<Float, Float, Float>>?,
    labelName: String,
    modifier: Modifier = Modifier
) {
    // Normalization: معالجة أحجام الفقاعات لتكون منطقية بصرياً
    val processedPoints = remember(dataPoints) {
        val list = dataPoints?.filter { it.first.isFinite() && it.second.isFinite() && it.third.isFinite() } ?: emptyList()
        val maxSize = list.maxOfOrNull { abs(it.third) } ?: 1f
        
        val sampled = if (list.size > 800) list.shuffled().take(800) else list
        
        sampled.map { 
            // جعل الحجم نسبي (بحد أقصى 20) لضمان دقة الرؤية
            Triple(it.first, it.second, (abs(it.third) / maxSize) * 25f + 2f)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize().padding(12.dp),
        factory = { context ->
            BubbleChart(context).apply {
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = Color.WHITE
                axisLeft.textColor = Color.WHITE
                axisRight.isEnabled = false
                legend.textColor = Color.WHITE
            }
        },
        update = { chart ->
            val finalPoints = processedPoints.filter { it.first.isFinite() && it.second.isFinite() && it.third.isFinite() }
            if (finalPoints.isEmpty()) {
                chart.clear()
                chart.setNoDataText("No valid numeric data to display")
                chart.invalidate()
                return@AndroidView
            }
            val entries = finalPoints.map { BubbleEntry(it.first, it.second, it.third) }
            val dataSet = BubbleDataSet(entries, labelName).apply {
                colors = listOf(Color.parseColor(GOLD_COLOR), Color.parseColor(GREEN_COLOR))
                setDrawValues(false)
            }
            chart.data = BubbleData(dataSet)
            chart.animateXY(1000, 1000)
            chart.invalidate()
        }
    )
}
