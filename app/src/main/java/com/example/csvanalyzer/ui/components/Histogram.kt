package com.example.csvanalyzer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.model.HistogramBin
import com.example.csvanalyzer.viewmodel.ColumnStatRow
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HistogramCompose(
    stat: ColumnStatRow,
    columnName: String,
    bins: List<HistogramBin>? = null
) {
    val gold = Color(0xFFEAA93A)
    val whiteC = Color(0xFFF8F8F8)
    val grayC = Color(0xFF9BA1A6)
    val gridC = Color.White.copy(alpha = 0.08f)
    val panelBg = Color(0xFF0F1727)
    val chipBg = Color(0xFF1A2235)
    val meanColor = Color(0xFF4DD0E1)
    val tooltipBg = Color(0xFF121A2C)

    val textMeasurer = rememberTextMeasurer()

    val binData: List<Triple<Float, Float, Int>> = when {
        !bins.isNullOrEmpty() -> bins.mapNotNull { b ->
            val start = b.binStart?.toFloat()
            val end = b.binEnd?.toFloat()
            val count = b.count
            if (start != null && end != null && count != null) Triple(start, end, count) else null
        }
        else -> buildFallbackBins(stat)
    }

    if (binData.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No data available for histogram", color = grayC, fontSize = 12.sp)
        }
        return
    }

    val maxCount = binData.maxOf { it.third }.coerceAtLeast(1)
    val minVal = binData.first().first
    val maxVal = binData.last().second
    val meanValue = stat.mean?.toFloat()

    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(binData) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    columnName,
                    color = whiteC,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Frequency Distribution", color = grayC, fontSize = 10.sp)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistogramActionChip("-") {
                    zoom = (zoom / 1.25f).coerceIn(1f, 6f)
                    if (zoom <= 1.01f) panX = 0f
                }
                HistogramActionChip("+") {
                    zoom = (zoom * 1.25f).coerceIn(1f, 6f)
                }
                HistogramActionChip("Reset") {
                    zoom = 1f
                    panX = 0f
                    selectedIndex = -1
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(0.03f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HistStat("Count", "${stat.count ?: 0}", gold)
            HistStat("Mean", formatShort(stat.mean?.toFloat() ?: 0f), meanColor)
            HistStat("Median", formatShort(stat.median?.toFloat() ?: 0f), whiteC)
            HistStat("Std", formatShort(stat.std?.toFloat() ?: 0f), whiteC)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(panelBg)
                .onSizeChanged { canvasSize = it }
        ) {
            if (selectedIndex in binData.indices && canvasSize != IntSize.Zero) {
                val width = canvasSize.width.toFloat()
                val height = canvasSize.height.toFloat()

                val leftPad = 54f
                val rightPad = 24f
                val topPad = 24f
                val bottomPad = 90f
                val plotWBase = width - leftPad - rightPad
                val plotW = plotWBase * zoom
                val plotH = height - topPad - bottomPad

                val barCount = binData.size
                val gap = 4f
                val barWidth = ((plotW - ((barCount - 1) * gap)) / barCount).coerceAtLeast(6f)

                val (_, _, count) = binData[selectedIndex]
                val x = leftPad + selectedIndex * (barWidth + gap) + panX
                val barH = (count.toFloat() / maxCount) * plotH * animatedProgress.value
                val y = topPad + plotH - barH

                val centerX = (x + barWidth / 2f).coerceIn(40f, width - 40f)

                Column(
                    modifier = Modifier
                        .offset(
                            x = (centerX - 48f).dp,
                            y = (y - 56f).coerceAtLeast(6f).dp
                        )
                        .background(tooltipBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${formatShort(binData[selectedIndex].first)} - ${formatShort(binData[selectedIndex].second)}",
                        color = whiteC,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Count: ${binData[selectedIndex].third}",
                        color = gold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(binData, zoom, panX, animatedProgress.value) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (zoom > 1f) {
                                    panX += dragAmount.x
                                }
                            },
                            onDragEnd = {
                                val width = size.width
                                val leftPad = 48f
                                val rightPad = 18f
                                val plotWBase = width - leftPad - rightPad
                                val plotW = plotWBase * zoom
                                val minPan = minOf(0f, plotWBase - plotW)
                                panX = panX.coerceIn(minPan, 0f)
                            }
                        )
                    }
                    .pointerInput(binData, zoom, panX) {
                        detectDragGestures(
                            onDragStart = { touch ->
                                val width = size.width
                                val height = size.height

                                val leftPad = 48f
                                val rightPad = 18f
                                val topPad = 18f
                                val bottomPad = 48f

                                val plotWBase = width - leftPad - rightPad
                                val plotW = plotWBase * zoom
                                val plotH = height - topPad - bottomPad

                                val barCount = binData.size
                                val gap = 4f
                                val barWidth = ((plotW - ((barCount - 1) * gap)) / barCount).coerceAtLeast(6f)

                                val tappedIndex = ((touch.x - leftPad - panX) / (barWidth + gap)).toInt()
                                selectedIndex = if (tappedIndex in 0 until barCount) tappedIndex else -1
                            },
                            onDrag = { _, _ -> }
                        )
                    }
            ) {
                val leftPad = 54f
                val rightPad = 24f
                val topPad = 24f
                val bottomPad = 90f

                val plotWBase = size.width - leftPad - rightPad
                val plotW = plotWBase * zoom
                val plotH = size.height - topPad - bottomPad

                val ySteps = 4
                for (i in 0..ySteps) {
                    val y = topPad + plotH - (plotH * i / ySteps)
                    drawLine(
                        color = gridC,
                        start = Offset(leftPad, y),
                        end = Offset(size.width - rightPad, y),
                        strokeWidth = 1f,
                        pathEffect = if (i > 0) PathEffect.dashPathEffect(floatArrayOf(4f, 6f)) else null
                    )

                    val labelVal = (maxCount * i / ySteps)
                    val txt = formatShort(labelVal.toFloat())
                    val layout = textMeasurer.measure(
                        text = txt,
                        style = TextStyle(color = grayC, fontSize = 9.sp)
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(leftPad - layout.size.width - 6f, y - layout.size.height / 2f)
                    )
                }

                val barCount = binData.size
                val gap = 4f
                val barWidth = ((plotW - ((barCount - 1) * gap)) / barCount).coerceAtLeast(6f)

                binData.forEachIndexed { i, (start, end, count) ->
                    val barH = (count.toFloat() / maxCount) * plotH * animatedProgress.value
                    val x = leftPad + i * (barWidth + gap) + panX
                    val y = topPad + plotH - barH

                    if (x + barWidth >= leftPad && x <= size.width - rightPad) {
                        val isSelected = i == selectedIndex

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (isSelected) {
                                    listOf(Color(0xFFFFC857), gold)
                                } else {
                                    listOf(gold, gold.copy(alpha = 0.5f))
                                },
                                startY = y,
                                endY = y + barH
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(5f, 5f)
                        )

                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.75f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(5f, 5f),
                                style = Stroke(width = 1.5f)
                            )
                        }

                        if (barH > 26f && barWidth > 22f) {
                            val cntTxt = formatShort(count.toFloat())
                            val cntLayout = textMeasurer.measure(
                                text = cntTxt,
                                style = TextStyle(
                                    color = whiteC,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = cntLayout,
                                topLeft = Offset(
                                    x + barWidth / 2f - cntLayout.size.width / 2f,
                                    y + 4f
                                )
                            )
                        }
                    }
                }

                drawLine(
                    color = Color.White.copy(alpha = 0.30f),
                    start = Offset(leftPad, topPad + plotH),
                    end = Offset(size.width - rightPad, topPad + plotH),
                    strokeWidth = 1.5f
                )

                val labelStep = when {
                    barCount <= 6 -> 1
                    barCount <= 12 -> 2
                    zoom >= 2f -> 1
                    else -> 3
                }

                binData.forEachIndexed { i, (start, _, _) ->
                    if (i % labelStep == 0) {
                        val labelX = leftPad + i * (barWidth + gap) + barWidth / 2f + panX
                        if (labelX in leftPad..(size.width - rightPad)) {
                            val txt = formatShort(start)
                            val layout = textMeasurer.measure(
                                text = txt,
                                style = TextStyle(color = grayC, fontSize = 9.sp)
                            )
                            drawText(
                                textLayoutResult = layout,
                                topLeft = Offset(
                                    labelX - layout.size.width / 2f,
                                    topPad + plotH + 8f
                                )
                            )
                        }
                    }
                }

                val lastX = leftPad + plotW + panX
                val lastTxt = formatShort(maxVal)
                val lastLayout = textMeasurer.measure(
                    text = lastTxt,
                    style = TextStyle(color = grayC, fontSize = 9.sp)
                )
                if (lastX - lastLayout.size.width in leftPad..size.width) {
                    drawText(
                        textLayoutResult = lastLayout,
                        topLeft = Offset(
                            lastX - lastLayout.size.width,
                            topPad + plotH + 8f
                        )
                    )
                }

                if (meanValue != null && maxVal > minVal) {
                    val meanRatio = ((meanValue - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                    val meanX = leftPad + (meanRatio * plotW) + panX

                    if (meanX in leftPad..(size.width - rightPad)) {
                        drawLine(
                            color = meanColor,
                            start = Offset(meanX, topPad),
                            end = Offset(meanX, topPad + plotH),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )

                        drawCircle(
                            color = meanColor,
                            radius = 5f,
                            center = Offset(meanX, topPad + 10f)
                        )

                        val meanLabel = textMeasurer.measure(
                            text = "Mean",
                            style = TextStyle(
                                color = meanColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        drawText(
                            textLayoutResult = meanLabel,
                            topLeft = Offset(
                                meanX - meanLabel.size.width / 2f,
                                topPad - 14f
                            )
                        )
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Range: ${formatShort(minVal)} → ${formatShort(maxVal)}",
                color = grayC,
                fontSize = 10.sp
            )
            Text(
                text = "Zoom: ${zoom.formatZoom()}x",
                color = gold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HistogramActionChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A2235))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { _, _ -> }
                )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFEAA93A),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HistStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF9BA1A6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

private fun buildFallbackBins(stat: ColumnStatRow): List<Triple<Float, Float, Int>> {
    val min = stat.min?.toFloat() ?: return emptyList()
    val max = stat.max?.toFloat() ?: return emptyList()
    val count = stat.count ?: 100
    if (max <= min) return emptyList()

    val binsCount = 10
    val step = (max - min) / binsCount
    val q1 = stat.q1?.toFloat() ?: min
    val median = stat.median?.toFloat() ?: (min + max) / 2f
    val q3 = stat.q3?.toFloat() ?: max

    return (0 until binsCount).map { i ->
        val start = min + i * step
        val end = start + step
        val center = (start + end) / 2f
        val binCount = when {
            center < q1 -> (count * 0.10 / (binsCount * 0.25)).toInt()
            center < median -> (count * 0.30 / (binsCount * 0.25)).toInt()
            center < q3 -> (count * 0.35 / (binsCount * 0.25)).toInt()
            else -> (count * 0.15 / (binsCount * 0.25)).toInt()
        }.coerceAtLeast(1)
        Triple(start, end, binCount)
    }
}

private fun formatShort(v: Float): String {
    if (v.isNaN() || v.isInfinite()) return "—"
    val a = abs(v)
    return when {
        a >= 1_000_000 -> String.format("%.1fM", v / 1_000_000)
        a >= 1_000 -> String.format("%.1fK", v / 1_000)
        a >= 10 -> String.format("%.0f", v)
        a >= 1 -> String.format("%.1f", v)
        a > 0 -> String.format("%.2f", v)
        else -> "0"
    }
}

private fun Float.formatZoom(): String {
    return ((this * 10).roundToInt() / 10f).toString()
}