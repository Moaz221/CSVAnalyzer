package com.example.csvanalyzer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.components.*
import com.example.csvanalyzer.ui.theme.*

data class CsvData(
    val numericColumns: List<String>,
    val categoricalColumns: List<String>,
    val rows: List<Map<String, String>>
)

/* ============================================================
   Bar Chart Screen (Premium)
   ============================================================ */
@Composable
fun BarChartScreen(csvData: CsvData) {
    var categoryColumn by remember { mutableStateOf(csvData.categoricalColumns.firstOrNull()) }
    var valueColumn by remember { mutableStateOf(csvData.numericColumns.firstOrNull()) }
    var showSheet by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(if (categoryColumn != null && valueColumn != null) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (categoryColumn == null || valueColumn == null) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        // خلفية تقنية خفيفة
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOfNotNull(
                    categoryColumn?.let { "X: $it" },
                    valueColumn?.let { "Y: $it" }
                ),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (categoryColumn != null && valueColumn != null) {
                    val dataMap = remember(categoryColumn, valueColumn, csvData) {
                        buildAggregatedMap(csvData, categoryColumn!!, valueColumn!!)
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        BarChartCompose(dataMap = dataMap)
                    }
                } else {
                    EmptyHint()
                }
            }
        }
    }

    if (showSheet) {
        ColumnPickerSheet(
            title = "Bar Chart Setup",
            fields = listOf(
                ColumnField("Category (X-Axis)", csvData.categoricalColumns, categoryColumn) { categoryColumn = it },
                ColumnField("Value (Y-Axis)", csvData.numericColumns, valueColumn) { valueColumn = it }
            ),
            onDismiss = { showSheet = false }
        )
    }
}

/* ============================================================
   Pie Chart Screen (Premium)
   ============================================================ */
@Composable
fun PieChartScreen(csvData: CsvData) {
    var selectedColumn by remember { mutableStateOf(csvData.categoricalColumns.firstOrNull()) }
    var showSheet by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (selectedColumn != null) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (selectedColumn == null) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOfNotNull(selectedColumn?.let { "Column: $it" }),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (selectedColumn != null) {
                    val dataMap = remember(selectedColumn, csvData) {
                        buildFrequencyMap(csvData, selectedColumn!!)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        PieChartCompose(dataMap = dataMap)
                    }
                } else {
                    EmptyHint()
                }
            }
        }
    }

    if (showSheet) {
        ColumnPickerSheet(
            title = "Pie Chart Setup",
            fields = listOf(
                ColumnField("Dimension", csvData.categoricalColumns, selectedColumn) { selectedColumn = it }
            ),
            onDismiss = { showSheet = false }
        )
    }
}

/* ============================================================
   Scatter Chart Screen (Premium)
   ============================================================ */
@Composable
fun ScatterChartScreen(csvData: CsvData) {
    var xColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(0)) }
    var yColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(1)) }
    var showSheet by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (xColumn != null && yColumn != null) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (xColumn == null || yColumn == null) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOfNotNull(xColumn?.let { "X: $it" }, yColumn?.let { "Y: $it" }),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (xColumn != null && yColumn != null) {
                    val points = remember(xColumn, yColumn, csvData) {
                        buildXYPoints(csvData, xColumn!!, yColumn!!)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        ScatterChartCompose(points, "$xColumn vs $yColumn")
                    }
                } else {
                    EmptyHint()
                }
            }
        }
    }

    if (showSheet) {
        ColumnPickerSheet(
            title = "Scatter Chart Setup",
            fields = listOf(
                ColumnField("X-Axis", csvData.numericColumns, xColumn) { xColumn = it },
                ColumnField("Y-Axis", csvData.numericColumns.filter { it != xColumn }, yColumn) { yColumn = it }
            ),
            onDismiss = { showSheet = false }
        )
    }
}

/* ============================================================
   Line Chart Screen (Premium)
   ============================================================ */
@Composable
fun LineChartScreen(csvData: CsvData) {
    var xColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(0)) }
    var yColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(1)) }
    var showSheet by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (xColumn != null && yColumn != null) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (xColumn == null || yColumn == null) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOfNotNull(xColumn?.let { "X: $it" }, yColumn?.let { "Y: $it" }),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (xColumn != null && yColumn != null) {
                    val points = remember(xColumn, yColumn, csvData) {
                        buildXYPoints(csvData, xColumn!!, yColumn!!)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        LineChartCompose(points, "$yColumn over $xColumn")
                    }
                } else {
                    EmptyHint()
                }
            }
        }
    }

    if (showSheet) {
        ColumnPickerSheet(
            title = "Line Chart Setup",
            fields = listOf(
                ColumnField("X-Axis", csvData.numericColumns, xColumn) { xColumn = it },
                ColumnField("Y-Axis", csvData.numericColumns.filter { it != xColumn }, yColumn) { yColumn = it }
            ),
            onDismiss = { showSheet = false }
        )
    }
}

/* ============================================================
   Radar Chart Screen (Premium)
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarChartScreen(csvData: CsvData) {
    var selectedColumns by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSheet by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (selectedColumns.size >= 3) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (selectedColumns.size < 3) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOf("Selected: ${selectedColumns.size}"),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (selectedColumns.size >= 3) {
                    val dataMap = remember(selectedColumns, csvData) {
                        selectedColumns.associateWith { col ->
                            csvData.rows.mapNotNull { it[col]?.toDoubleOrNull() }.average()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        RadarChartCompose(dataMap = dataMap)
                    }
                } else {
                    EmptyHint("Select at least 3 columns for Radar chart")
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = SurfaceDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = GoldPremium) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("Radar Chart Setup", color = GoldPremium, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                
                MultiColumnSelector(
                    label = "Compare Columns (Min 3)",
                    columns = csvData.numericColumns,
                    selectedColumns = selectedColumns,
                    onSelectionChanged = { selectedColumns = it },
                    maxSelection = 8
                )

                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("APPLY SETTINGS", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/* ============================================================
   Bubble Chart Screen (Premium)
   ============================================================ */
@Composable
fun BubbleChartScreen(csvData: CsvData) {
    var xColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(0)) }
    var yColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(1)) }
    var sizeColumn by remember { mutableStateOf(csvData.numericColumns.getOrNull(2)) }
    var showSheet by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (xColumn != null && yColumn != null && sizeColumn != null) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) {
        if (xColumn == null || yColumn == null || sizeColumn == null) showSheet = true
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            drawCircle(GoldPremium, radius = 300.dp.toPx(), center = Offset(size.width, 0f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChartInfoBar(
                chips = listOfNotNull(xColumn?.let { "X: $it" }, yColumn?.let { "Y: $it" }, sizeColumn?.let { "Size: $it" }),
                onSettingsClick = { showSheet = true }
            )

            Box(modifier = Modifier.weight(1f).padding(16.dp).alpha(alpha)) {
                if (xColumn != null && yColumn != null && sizeColumn != null) {
                    val points = remember(xColumn, yColumn, sizeColumn, csvData) {
                        csvData.rows.mapNotNull { row ->
                            val x = row[xColumn]?.toFloatOrNull()
                            val y = row[yColumn]?.toFloatOrNull()
                            val s = row[sizeColumn]?.toFloatOrNull()
                            if (x != null && y != null && s != null) Triple(x, y, s) else null
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDark.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        BubbleChartCompose(points, "$xColumn vs $yColumn")
                    }
                } else {
                    EmptyHint()
                }
            }
        }
    }

    if (showSheet) {
        ColumnPickerSheet(
            title = "Bubble Chart Setup",
            fields = listOf(
                ColumnField("X-Axis", csvData.numericColumns, xColumn) { xColumn = it },
                ColumnField("Y-Axis", csvData.numericColumns.filter { it != xColumn }, yColumn) { yColumn = it },
                ColumnField("Bubble Size", csvData.numericColumns.filter { it != xColumn && it != yColumn }, sizeColumn) { sizeColumn = it }
            ),
            onDismiss = { showSheet = false }
        )
    }
}

/* ============================================================
   Helpers
   ============================================================ */
private fun buildAggregatedMap(
    csvData: CsvData,
    categoryCol: String,
    valueCol: String
): Map<String, Double> = csvData.rows
    .mapNotNull { row ->
        val cat = row[categoryCol] ?: return@mapNotNull null
        val v = row[valueCol]?.toDoubleOrNull() ?: return@mapNotNull null
        cat to v
    }
    .groupBy({ it.first }, { it.second })
    .mapValues { it.value.sum() }

private fun buildFrequencyMap(csvData: CsvData, col: String): Map<String, Int> =
    csvData.rows.mapNotNull { it[col] }.groupingBy { it }.eachCount()

private fun buildXYPoints(
    csvData: CsvData,
    xCol: String,
    yCol: String
): List<Pair<Float, Float>> = csvData.rows.mapNotNull { row ->
    val x = row[xCol]?.toFloatOrNull()
    val y = row[yCol]?.toFloatOrNull()
    if (x != null && y != null) x to y else null
}
