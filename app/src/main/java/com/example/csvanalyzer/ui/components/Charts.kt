package com.example.csvanalyzer.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.Locale
import kotlin.math.abs

private const val GOLD_COLOR = "#EAA93A"
private const val GOLD_LIGHT = "#F6C85F"
private const val TEXT_COLOR = "#F8F8F8"
private const val MUTED_TEXT = "#B8B8B8"
private const val GRID_COLOR = "#22FFFFFF"
private const val PANEL_DARK = "#111318"

private val PremiumPalette = listOf(
    AndroidColor.parseColor("#EAA93A"),
    AndroidColor.parseColor("#F6C85F"),
    AndroidColor.parseColor("#4CAF50"),
    AndroidColor.parseColor("#64B5F6"),
    AndroidColor.parseColor("#BA68C8"),
    AndroidColor.parseColor("#FF8A65"),
    AndroidColor.parseColor("#90A4AE"),
    AndroidColor.parseColor("#D4E157"),
    AndroidColor.parseColor("#26C6DA"),
    AndroidColor.parseColor("#EC407A")
)

private fun compactNumber(value: Float): String {
    val absValue = abs(value)
    return when {
        absValue >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000)
        absValue >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000)
        absValue >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000)
        value % 1f == 0f -> String.format(Locale.US, "%.0f", value)
        else -> String.format(Locale.US, "%.2f", value)
    }
}

private fun shortLabel(label: String, max: Int = 12): String {
    return if (label.length > max) label.take(max) + "…" else label
}

private class CompactNumberFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return compactNumber(value)
    }
}

private class PieSmartFormatter : ValueFormatter() {
    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        val label = pieEntry?.label.orEmpty()
        return "${shortLabel(label, 10)}\n${compactNumber(value)}"
    }
}

private class SmartMarkerView(context: Context) : MarkerView(context, android.R.layout.simple_list_item_1) {

    private val textView: TextView = findViewById(android.R.id.text1)

    init {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f
            setColor(AndroidColor.parseColor("#EE111318"))
            setStroke(2, AndroidColor.parseColor(GOLD_COLOR))
        }

        background = bg
        setPadding(18, 10, 18, 10)

        textView.setTextColor(AndroidColor.WHITE)
        textView.textSize = 12f
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.setSingleLine(false)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return

        val label = when (e) {
            is PieEntry -> e.label
            else -> e.data?.toString().orEmpty()
        }

        val title = if (label.isNotBlank()) label else "Value"
        textView.text = "$title\n${compactNumber(e.y)}"

        super.refreshContent(e, highlight)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return MPPointF(-(width / 2f), -height - 14f)
    }
}

private fun setupBaseChart(chart: Chart<*>) {
    chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    chart.description.isEnabled = false
    chart.setNoDataTextColor(AndroidColor.parseColor(GOLD_COLOR))
    chart.setNoDataText("No data available")
    chart.setTouchEnabled(true)
    chart.setDrawMarkers(true)
    chart.setExtraOffsets(12f, 12f, 12f, 18f)

    chart.legend.apply {
        isEnabled = true
        textColor = AndroidColor.parseColor(MUTED_TEXT)
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        form = Legend.LegendForm.CIRCLE
        formSize = 9f
        verticalAlignment = Legend.LegendVerticalAlignment.TOP
        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        orientation = Legend.LegendOrientation.HORIZONTAL
        setDrawInside(false)
        setWordWrapEnabled(true)
    }

    if (chart is BarLineChartBase<*>) {
        chart.setBackgroundColor(AndroidColor.TRANSPARENT)
        chart.setPinchZoom(true)
        chart.setScaleEnabled(true)
        chart.isDragEnabled = true
        chart.isDoubleTapToZoomEnabled = true

        chart.xAxis.apply {
            textColor = AndroidColor.parseColor(TEXT_COLOR)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gridColor = AndroidColor.parseColor(GRID_COLOR)
            axisLineColor = AndroidColor.parseColor(GRID_COLOR)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        chart.axisLeft.apply {
            textColor = AndroidColor.parseColor(MUTED_TEXT)
            textSize = 10f
            gridColor = AndroidColor.parseColor(GRID_COLOR)
            axisLineColor = AndroidColor.TRANSPARENT
            valueFormatter = CompactNumberFormatter()
            setDrawAxisLine(false)
        }

        chart.axisRight.isEnabled = false
    }
}

/* 1) Bar Chart - Top 10 واضحين بالقيم */
@Composable
fun BarChartCompose(dataMap: Map<String, Double>) {
    val displayData = remember(dataMap) {
        dataMap
            .filter { it.value.isFinite() }
            .entries
            .sortedByDescending { it.value }
            .take(10)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            BarChart(context).apply {
                setupBaseChart(this)
                marker = SmartMarkerView(context)
                animateY(900, Easing.EaseOutCubic)
            }
        },
        update = { chart ->
            if (displayData.isEmpty()) {
                chart.clear()
                chart.setNoDataText("No valid numeric data to display")
                chart.invalidate()
                return@AndroidView
            }

            val entries = displayData.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.value.toFloat()).apply {
                    data = item.key
                }
            }

            val dataSet = BarDataSet(entries, "Top Values").apply {
                color = AndroidColor.parseColor(GOLD_COLOR)
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 10f
                valueTypeface = Typeface.DEFAULT_BOLD
                valueFormatter = CompactNumberFormatter()
                highLightColor = AndroidColor.parseColor(GOLD_LIGHT)
            }

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(displayData.map { shortLabel(it.key) })
                labelCount = displayData.size
                labelRotationAngle = -35f
            }

            if (displayData.all { it.value >= 0 }) {
                chart.axisLeft.axisMinimum = 0f
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.62f
            }

            chart.setFitBars(true)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

/* 6) Scatter Chart + Regression Line */
@Composable
fun ScatterChartCompose(
    dataPoints: List<Pair<Float, Float>>,
    labelName: String
) {
    val sampledPoints = remember(dataPoints) {
        val clean = dataPoints.filter { it.first.isFinite() && it.second.isFinite() }

        if (clean.size <= 10000) {
            clean.sortedBy { it.first }
        } else {
            val step = (clean.size / 8000).coerceAtLeast(1)
            clean.filterIndexed { index, _ -> index % step == 0 }
                .take(8000)
                .sortedBy { it.first }
        }
    }

    // حساب خط الانحدار (y = mx + c)
    val regressionLine = remember(sampledPoints) {
        if (sampledPoints.size < 2) return@remember null
        val n = sampledPoints.size.toFloat()
        val sumX = sampledPoints.sumOf { it.first.toDouble() }.toFloat()
        val sumY = sampledPoints.sumOf { it.second.toDouble() }.toFloat()
        val sumXY = sampledPoints.sumOf { (it.first * it.second).toDouble() }.toFloat()
        val sumX2 = sampledPoints.sumOf { (it.first * it.first).toDouble() }.toFloat()

        val denom = (n * sumX2 - sumX * sumX)
        if (denom == 0f) return@remember null

        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n
        slope to intercept
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            CombinedChart(context).apply {
                setupBaseChart(this)

                marker = SmartMarkerView(context)
                description.isEnabled = false
                
                // ترتيب الرسم: الخط فوق النقط
                setDrawOrder(arrayOf(
                    CombinedChart.DrawOrder.SCATTER,
                    CombinedChart.DrawOrder.LINE
                ))

                legend.apply {
                    isEnabled = true
                    textColor = AndroidColor.parseColor("#B8C1CC")
                    textSize = 11f
                    form = Legend.LegendForm.CIRCLE
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                }

                xAxis.apply {
                    textColor = AndroidColor.parseColor("#9BA1A6")
                    gridColor = AndroidColor.parseColor("#22FFFFFF")
                    setDrawAxisLine(true)
                    axisLineColor = AndroidColor.parseColor("#33FFFFFF")
                    setDrawGridLines(true)
                    position = XAxis.XAxisPosition.BOTTOM
                }

                axisLeft.apply {
                    textColor = AndroidColor.parseColor("#9BA1A6")
                    gridColor = AndroidColor.parseColor("#22FFFFFF")
                    setDrawGridLines(true)
                }

                axisRight.isEnabled = false
                
                setTouchEnabled(true)
                setPinchZoom(true)
                isDragEnabled = true
                isScaleXEnabled = true
                isScaleYEnabled = true

                animateXY(900, 900)
            }
        },
        update = { chart ->
            if (sampledPoints.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val combinedData = CombinedData()

            // 1. إضافة النقط (Scatter)
            val scatterEntries = sampledPoints.map { Entry(it.first, it.second) }
            val scatterDataSet = ScatterDataSet(scatterEntries, labelName).apply {
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                scatterShapeSize = if (scatterEntries.size > 1000) 7f else 10f
                color = AndroidColor.parseColor("#EAA93A")
                setDrawValues(false)
                highLightColor = AndroidColor.WHITE
            }
            combinedData.setData(ScatterData(scatterDataSet))

            // 2. إضافة خط الانحدار (Line)
            regressionLine?.let { (slope, intercept) ->
                val minX = sampledPoints.minOf { it.first }
                val maxX = sampledPoints.maxOf { it.first }
                
                val lineEntries = listOf(
                    Entry(minX, slope * minX + intercept),
                    Entry(maxX, slope * maxX + intercept)
                )

                val lineDataSet = LineDataSet(lineEntries, "Regression Line").apply {
                    color = AndroidColor.parseColor("#64B5F6") // أزرق مريح للعين
                    lineWidth = 3f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                    enableDashedLine(10f, 5f, 0f) // خط مقطع ليكون شكله احترافي
                }
                combinedData.setData(LineData(lineDataSet))
            }

            chart.data = combinedData

            // ضبط حدود المحاور
            val minX = sampledPoints.minOf { it.first }
            val maxX = sampledPoints.maxOf { it.first }
            val rangeX = (maxX - minX).coerceAtLeast(1f)
            chart.xAxis.axisMinimum = minX - rangeX * 0.05f
            chart.xAxis.axisMaximum = maxX + rangeX * 0.05f

            val minY = sampledPoints.minOf { it.second }
            val maxY = sampledPoints.maxOf { it.second }
            val rangeY = (maxY - minY).coerceAtLeast(1f)
            chart.axisLeft.axisMinimum = minY - rangeY * 0.05f
            chart.axisLeft.axisMaximum = maxY + rangeY * 0.05f

            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

/* 2) Pie Chart - Top 7 + Others مع أرقام واضحة */
@Composable
fun PieChartCompose(dataMap: Map<String, Int>) {
    val processedData = remember(dataMap) {
        val sorted = dataMap
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.value }

        if (sorted.size > 10) {
            val top = sorted.take(9).toMutableList()
            val othersSum = sorted.drop(9).sumOf { it.value }
            top.add(java.util.AbstractMap.SimpleEntry("Others", othersSum))
            top
        } else {
            sorted
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            PieChart(context).apply {
                setupBaseChart(this)
                marker = SmartMarkerView(context)

                setUsePercentValues(false)
                setDrawEntryLabels(false) // نعتمد على الـ ValueFormatter للخارج
                
                // هوامش إضافية عشان الـ Labels اللي بره متتقصش
                setExtraOffsets(25f, 5f, 25f, 5f)
                minOffset = 0f

                holeRadius = 58f
                transparentCircleRadius = 62f
                setHoleColor(AndroidColor.TRANSPARENT)

                setCenterTextColor(AndroidColor.WHITE)
                setCenterTextSize(13f)
                setCenterTextTypeface(Typeface.DEFAULT_BOLD)

                // تحسين مكان المفتاح (Legend) للـ Pie Chart
                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    yOffset = 2f
                    setWordWrapEnabled(true)
                }

                animateY(1000, Easing.EaseOutCubic)
            }
        },
        update = { chart ->
            if (processedData.isEmpty()) {
                chart.clear()
                chart.setNoDataText("No categorical data available")
                chart.invalidate()
                return@AndroidView
            }

            val total = processedData.sumOf { it.value }

            val entries = processedData.map {
                PieEntry(it.value.toFloat(), it.key).apply {
                    data = it.key
                }
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = PremiumPalette
                sliceSpace = 3f
                selectionShift = 8f

                valueTextColor = AndroidColor.WHITE
                valueTextSize = 10f
                valueTypeface = Typeface.DEFAULT_BOLD
                valueFormatter = PieSmartFormatter()

                // إظهار الداتا بره الشريحة بخطوط وصل
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueLineColor = AndroidColor.parseColor("#AAFFFFFF")
                valueLineWidth = 1.5f
                valueLinePart1OffsetPercentage = 75f
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.4f
            }

            chart.centerText = "Total\n${compactNumber(total.toFloat())}"
            chart.data = PieData(dataSet)

            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

/* 3) Radar Chart - مناسب للمقارنة بين كذا Metric */
@Composable
fun RadarChartCompose(dataMap: Map<String, Double>) {
    val validData = remember(dataMap) {
        dataMap
            .filter { it.value.isFinite() }
            .entries
            .sortedByDescending { it.value }
            .take(8)
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .transformable(state = state)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .padding(8.dp),
            factory = { context ->
                RadarChart(context).apply {
                    setupBaseChart(this)
                    marker = SmartMarkerView(context)

                    webColor = AndroidColor.parseColor("#66FFFFFF")
                    webColorInner = AndroidColor.parseColor("#33FFFFFF")
                    webAlpha = 120
                    skipWebLineCount = 0

                    xAxis.apply {
                        textColor = AndroidColor.WHITE
                        textSize = 10f
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    yAxis.apply {
                        textColor = AndroidColor.parseColor(MUTED_TEXT)
                        textSize = 9f
                        setDrawLabels(false)
                        axisMinimum = 0f
                    }

                    animateY(1100, Easing.EaseOutCubic)
                }
            },
            update = { chart ->
                if (validData.isEmpty()) {
                    chart.clear()
                    chart.setNoDataText("No valid numeric data to display")
                    chart.invalidate()
                    return@AndroidView
                }

                val entries = validData.map {
                    RadarEntry(it.value.toFloat()).apply {
                        data = it.key
                    }
                }

                val dataSet = RadarDataSet(entries, "Analysis Scale").apply {
                    color = AndroidColor.parseColor(GOLD_COLOR)
                    fillColor = AndroidColor.parseColor(GOLD_COLOR)
                    setDrawFilled(true)
                    fillAlpha = 95
                    lineWidth = 2.5f

                    valueTextColor = AndroidColor.WHITE
                    valueTextSize = 9f
                    valueTypeface = Typeface.DEFAULT_BOLD
                    valueFormatter = CompactNumberFormatter()
                }

                chart.xAxis.valueFormatter = IndexAxisValueFormatter(validData.map { shortLabel(it.key, 9) })
                chart.data = RadarData(dataSet)

                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

/* 4) Line Chart - Trend واضح مع نقاط وقيم */
@Composable
fun LineChartCompose(dataPoints: List<Pair<Float, Float>>, labelName: String) {
    val processedPoints = remember(dataPoints) {
        val list = dataPoints
            .filter { it.second.isFinite() }
            .sortedBy { it.first }
        
        if (list.size > 1000) {
            // نأخذ عينة منتظمة للحفاظ على شكل الـ Trend
            val step = list.size / 1000
            list.filterIndexed { index, _ -> index % step == 0 }
        } else {
            list
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            LineChart(context).apply {
                setupBaseChart(this)
                marker = SmartMarkerView(context)

                // تفعيل التقريب والتحريك
                setPinchZoom(true)
                setScaleEnabled(true)
                isDragEnabled = true

                xAxis.valueFormatter = CompactNumberFormatter()
                animateX(900, Easing.EaseOutCubic)
            }
        },
        update = { chart ->
            if (processedPoints.isEmpty()) {
                chart.clear()
                chart.setNoDataText("No valid numeric data to display")
                chart.invalidate()
                return@AndroidView
            }

            val entries = processedPoints.map {
                Entry(it.first, it.second).apply {
                    data = "X: ${compactNumber(it.first)}"
                }
            }

            val dataSet = LineDataSet(entries, labelName).apply {
                color = AndroidColor.parseColor(GOLD_COLOR)
                lineWidth = 2.5f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.15f

                setDrawCircles(processedPoints.size < 100)
                setCircleColor(AndroidColor.WHITE)
                circleRadius = 3.5f
                circleHoleRadius = 1.8f
                circleHoleColor = AndroidColor.parseColor(GOLD_COLOR)

                setDrawFilled(true)
                fillAlpha = 50
                fillColor = AndroidColor.parseColor(GOLD_COLOR)

                setDrawValues(processedPoints.size < 50)
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 9f
                valueFormatter = CompactNumberFormatter()

                highLightColor = AndroidColor.parseColor(GOLD_LIGHT)
                setDrawHorizontalHighlightIndicator(false)
            }

            chart.data = LineData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

/* 5) Horizontal Bar Chart - الأفضل للأسماء الطويلة */
@Composable
fun HorizontalBarChartCompose(dataMap: Map<String, Double>) {
    val displayData = remember(dataMap) {
        dataMap
            .filter { it.value.isFinite() }
            .entries
            .sortedByDescending { it.value }
            .take(10)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            HorizontalBarChart(context).apply {
                setupBaseChart(this)
                marker = SmartMarkerView(context)
                animateY(900, Easing.EaseOutCubic)
            }
        },
        update = { chart ->
            if (displayData.isEmpty()) {
                chart.clear()
                chart.setNoDataText("No valid numeric data to display")
                chart.invalidate()
                return@AndroidView
            }

            val entries = displayData.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.value.toFloat()).apply {
                    data = item.key
                }
            }

            val dataSet = BarDataSet(entries, "Real Values").apply {
                colors = PremiumPalette
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 10f
                valueTypeface = Typeface.DEFAULT_BOLD
                valueFormatter = CompactNumberFormatter()
                highLightColor = AndroidColor.parseColor(GOLD_LIGHT)
            }

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(displayData.map { shortLabel(it.key, 16) })
                labelCount = displayData.size
                textSize = 10f
            }

            if (displayData.all { it.value >= 0 }) {
                chart.axisLeft.axisMinimum = 0f
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.64f
            }

            chart.setFitBars(true)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}