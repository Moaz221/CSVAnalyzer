package com.example.csvanalyzer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class BoxPlotData(
    val min: Float,
    val q1: Float,
    val median: Float,
    val q3: Float,
    val max: Float,
    val outliers: List<Float>,
    val label: String,
    val mean: Float? = null
)

/* ── Single source of truth for plot geometry ── */
private fun plotToX(
    value: Float,
    width: Float,
    zoom: Float,
    panX: Float,
    paddedMin: Float,
    paddedMax: Float
): Float {
    val leftPad = 45f
    val rightPad = 30f
    val plotWBase = width - leftPad - rightPad
    val plotW = plotWBase * zoom
    val range = (paddedMax - paddedMin).takeIf { it > 0f } ?: 1f
    val normalized = ((value - paddedMin) / range).coerceIn(0f, 1f)
    return leftPad + normalized * plotW + panX
}

@Composable
fun ProfessionalBoxPlotCompose(
    data: BoxPlotData,
    modifier: Modifier = Modifier
) {
    /* ══ Theme tokens (gold / black match the rest of the app) ══ */
    val bg               = Color(0xFF0C0D11)
    val panel            = Color(0xFF13141A)
    val chipBg           = Color(0xFF171921)
    val textPrimary      = Color(0xFFF8F8F8)
    val textSecondary    = Color(0xFFB0A594)   // warm gray, fits gold
    val gold             = Color(0xFFEAA93A)   // AppColors.GoldPremium
    val goldSoft         = gold.copy(alpha = 0.22f)
    val whiskerColor     = Color(0xFFFFD166)
    val medianColor      = Color(0xFFFFFFFF)
    val outlierColor     = Color(0xFFFF5A5F)
    val meanColor        = Color(0xFF4DD0E1)
    val gridColor        = Color.White.copy(alpha = 0.08f)
    val axisColor        = Color.White.copy(alpha = 0.18f)
    val tooltipBg        = Color(0xFF181A26)
    val tooltipBorder    = gold.copy(alpha = 0.45f)

    val iqr = data.q3 - data.q1
    val lowerWhisker = if (iqr > 0f) max(data.min, data.q1 - 1.5f * iqr) else data.min
    val upperWhisker = if (iqr > 0f) min(data.max, data.q3 + 1.5f * iqr) else data.max

    val visibleMin = min(lowerWhisker, data.outliers.minOrNull() ?: lowerWhisker)
    val visibleMax = max(upperWhisker, data.outliers.maxOrNull() ?: upperWhisker)
    val rawRange = (visibleMax - visibleMin).takeIf { it > 0f } ?: 1f
    val paddedMin = visibleMin - rawRange * 0.08f
    val paddedMax = visibleMax + rawRange * 0.08f

    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
    }

    val points = remember(data) {
        buildList {
            add("Min" to data.min)
            add("Lower Whisker" to lowerWhisker)
            add("Q1" to data.q1)
            add("Median" to data.median)
            add("Q3" to data.q3)
            add("Upper Whisker" to upperWhisker)
            add("Max" to data.max)
            data.mean?.let { add("Mean" to it) }
            data.outliers.forEachIndexed { i, v -> add("Outlier ${i + 1}" to v) }
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, gold.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF12141A), Color(0xFF0C0D11))
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            /* ── Header ── */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = data.label,
                        color = textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Interactive box plot • drag to pan / select",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoxPlotActionChip("–") {
                        zoom = (zoom / 1.25f).coerceIn(1f, 6f)
                        if (zoom <= 1.01f) panX = 0f
                    }
                    BoxPlotActionChip("+") {
                        zoom = (zoom * 1.25f).coerceIn(1f, 6f)
                    }
                    BoxPlotActionChip("Reset") {
                        zoom = 1f
                        panX = 0f
                        selectedIndex = -1
                    }
                }
            }

            /* ── Legend ── */
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoxPlotLegendChip("IQR", gold)
                BoxPlotLegendChip("Median", medianColor)
                BoxPlotLegendChip("Mean", meanColor)
                BoxPlotLegendChip("Outlier", outlierColor)
                BoxPlotLegendChip("Whisker", whiskerColor)
            }

            /* ── Stats chips ── */
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoxPlotMiniStat("MIN", formatSmart(data.min), chipBg, textSecondary, textPrimary)
                BoxPlotMiniStat("Q1",  formatSmart(data.q1),  chipBg, textSecondary, textPrimary)
                BoxPlotMiniStat("MED", formatSmart(data.median), chipBg, textSecondary, gold)
                BoxPlotMiniStat("Q3",  formatSmart(data.q3),  chipBg, textSecondary, textPrimary)
                BoxPlotMiniStat("MAX", formatSmart(data.max), chipBg, textSecondary, textPrimary)
                BoxPlotMiniStat("IQR", formatSmart(iqr),    chipBg, textSecondary, textPrimary)
                data.mean?.let {
                    BoxPlotMiniStat("MEAN", formatSmart(it), chipBg, textSecondary, meanColor)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            /* ── Canvas area ── */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(panel)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .pointerInput(data, zoom, panX) {
                            detectDragGestures(
                                onDragStart = { touch ->
                                    val w = size.width.toFloat()
                                    val nearest = points.withIndex().minByOrNull {
                                        val px = plotToX(it.value.second, w, zoom, panX, paddedMin, paddedMax)
                                        abs(px - touch.x)
                                    }
                                    selectedIndex = if (nearest != null && abs(
                                            plotToX(nearest.value.second, w, zoom, panX, paddedMin, paddedMax) - touch.x
                                        ) < 30f) nearest.index else -1
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (zoom > 1f) panX += dragAmount.x
                                },
                                onDragEnd = {
                                    val w = size.width.toFloat()
                                    val plotWBase = w - 45f - 30f
                                    val plotW = plotWBase * zoom
                                    val minPan = minOf(0f, plotWBase - plotW)
                                    panX = panX.coerceIn(minPan, 0f)
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val leftPad = 45f
                    val rightPad = 30f
                    val topPad = 30f
                    val bottomPad = 90f
                    val plotH = height - topPad - bottomPad
                    val centerY = topPad + plotH * 0.48f
                    val boxHeight = 85f * drawProgress.value
                    val range = (paddedMax - paddedMin).takeIf { it > 0f } ?: 1f

                    fun toX(v: Float): Float = plotToX(v, width, zoom, panX, paddedMin, paddedMax)

                    val xMin = toX(data.min)
                    val xLW = toX(lowerWhisker)
                    val xQ1 = toX(data.q1)
                    val xMed = toX(data.median)
                    val xQ3 = toX(data.q3)
                    val xUW = toX(upperWhisker)
                    val xMax = toX(data.max)
                    val xMean = data.mean?.let { toX(it) }

                    /* Grid */
                    val gridSteps = 5
                    for (i in 0..gridSteps) {
                        val value = paddedMin + (range * i / gridSteps)
                        val x = toX(value)
                        drawLine(
                            color = gridColor,
                            start = Offset(x, topPad),
                            end = Offset(x, topPad + plotH),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                        )
                        val lbl = formatSmart(value)
                        val layout = textMeasurer.measure(
                            text = lbl,
                            style = TextStyle(color = textSecondary, fontSize = 10.sp)
                        )
                        drawText(
                            textLayoutResult = layout,
                            topLeft = Offset(
                                x - layout.size.width / 2f,
                                topPad + plotH + 10f
                            )
                        )
                    }

                    /* Axis */
                    drawLine(
                        color = axisColor,
                        start = Offset(leftPad, topPad + plotH),
                        end = Offset(width - rightPad, topPad + plotH),
                        strokeWidth = 1.5f
                    )

                    /* Whiskers */
                    drawLine(
                        color = whiskerColor,
                        start = Offset(xLW, centerY),
                        end = Offset(xQ1, centerY),
                        strokeWidth = 3.5f * drawProgress.value
                    )
                    drawLine(
                        color = whiskerColor,
                        start = Offset(xQ3, centerY),
                        end = Offset(xUW, centerY),
                        strokeWidth = 3.5f * drawProgress.value
                    )
                    val cap = 16f * drawProgress.value
                    listOf(xLW, xUW).forEach { x ->
                        drawLine(
                            color = whiskerColor,
                            start = Offset(x, centerY - cap),
                            end = Offset(x, centerY + cap),
                            strokeWidth = 3.5f
                        )
                    }

                    /* Box IQR */
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(gold.copy(alpha = 0.32f), goldSoft)
                        ),
                        topLeft = Offset(xQ1, centerY - boxHeight / 2f),
                        size = Size((xQ3 - xQ1).coerceAtLeast(8f), boxHeight),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = gold,
                        topLeft = Offset(xQ1, centerY - boxHeight / 2f),
                        size = Size((xQ3 - xQ1).coerceAtLeast(8f), boxHeight),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 2.5f)
                    )

                    /* Median */
                    drawLine(
                        color = medianColor,
                        start = Offset(xMed, centerY - boxHeight / 2f),
                        end = Offset(xMed, centerY + boxHeight / 2f),
                        strokeWidth = 4f
                    )
                    drawCircle(color = gold, radius = 6f, center = Offset(xMed, centerY))
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(xMed, centerY),
                        style = Stroke(width = 1.8f)
                    )

                    /* Mean line */
                    xMean?.let { mx ->
                        drawLine(
                            color = meanColor,
                            start = Offset(mx, topPad),
                            end = Offset(mx, topPad + plotH),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                        drawCircle(color = meanColor, radius = 5f, center = Offset(mx, topPad + 12f))
                        val lbl = textMeasurer.measure(
                            text = "Mean",
                            style = TextStyle(color = meanColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = lbl,
                            topLeft = Offset(mx - lbl.size.width / 2f, topPad - 14f)
                        )
                    }

                    /* Min / Max caps */
                    listOf(xMin, xMax).forEach { x ->
                        drawCircle(
                            color = whiskerColor.copy(alpha = 0.2f),
                            radius = 7f,
                            center = Offset(x, centerY)
                        )
                        drawCircle(color = whiskerColor, radius = 3.5f, center = Offset(x, centerY))
                    }

                    /* Outliers */
                    data.outliers.forEach { outlier ->
                        if (outlier < lowerWhisker || outlier > upperWhisker) {
                            val x = toX(outlier)
                            drawCircle(
                                color = outlierColor.copy(alpha = 0.2f),
                                radius = 8f,
                                center = Offset(x, centerY)
                            )
                            drawCircle(
                                color = outlierColor,
                                radius = 4.5f,
                                center = Offset(x, centerY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.5f,
                                center = Offset(x, centerY),
                                style = Stroke(width = 1.2f)
                            )
                        }
                    }

                    /* Selection vertical line */
                    if (selectedIndex in points.indices) {
                        val selX = toX(points[selectedIndex].second).coerceIn(10f, width - 10f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(selX, topPad),
                            end = Offset(selX, topPad + plotH),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )

                        /* ── Tooltip drawn INSIDE canvas (no more external offset bug) ── */
                        val sel = points[selectedIndex]
                        val tooltipW = 110f
                        val tooltipH = 40f
                        val tooltipX = (selX - tooltipW / 2f).coerceIn(8f, width - tooltipW - 8f)
                        val tooltipY = 8f

                        drawRoundRect(
                            color = tooltipBg,
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipW, tooltipH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = tooltipBorder,
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipW, tooltipH),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 1f)
                        )

                        val nameLayout = textMeasurer.measure(
                            text = sel.first,
                            style = TextStyle(color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        )
                        val valLayout = textMeasurer.measure(
                            text = formatSmart(sel.second),
                            style = TextStyle(color = gold, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        )

                        drawText(
                            textLayoutResult = nameLayout,
                            topLeft = Offset(
                                tooltipX + (tooltipW - nameLayout.size.width) / 2f,
                                tooltipY + 5f
                            )
                        )
                        drawText(
                            textLayoutResult = valLayout,
                            topLeft = Offset(
                                tooltipX + (tooltipW - valLayout.size.width) / 2f,
                                tooltipY + 22f
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Range: ${formatSmart(paddedMin)} → ${formatSmart(paddedMax)}",
                    color = textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Zoom: ${zoom.formatZoom()}x",
                    color = gold,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* ── Sub-components ── */

@Composable
private fun BoxPlotActionChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF181A24))
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = text,
                color = Color(0xFFEAA93A),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BoxPlotLegendChip(
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(8.dp).background(color, CircleShape)
        )
        Text(
            text = text,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BoxPlotMiniStat(
    label: String,
    value: String,
    bg: Color,
    labelColor: Color,
    valueColor: Color
) {
    Column(
        modifier = Modifier
            .widthIn(min = 68.dp)
            .background(bg, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun formatSmart(v: Float): String {
    if (v.isNaN() || v.isInfinite()) return "—"
    val a = abs(v)
    return when {
        a >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", v / 1_000_000f)
        a >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", v / 1_000f)
        a >= 100 -> String.format(java.util.Locale.US, "%.0f", v)
        a >= 10 -> String.format(java.util.Locale.US, "%.1f", v)
        a >= 1 -> String.format(java.util.Locale.US, "%.2f", v)
        a > 0 -> String.format(java.util.Locale.US, "%.3f", v)
        else -> "0"
    }
}

private fun Float.formatZoom(): String {
    return ((this * 10).roundToInt() / 10f).toString()
}