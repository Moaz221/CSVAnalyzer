package com.example.csvanalyzer.ui.screens.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.components.*
import com.example.csvanalyzer.viewmodel.*
import kotlin.math.min

// ─────────────────────────────────────────────────────────────
// DESIGN TOKENS
// ─────────────────────────────────────────────────────────────
object AppColors {
    val BgDark        = Color(0xFF0A0C10)
    val BgMidDark     = Color(0xFF0D1426)
    val BgBottomDark  = Color(0xFF111A31)
    val BgTopLight    = Color(0xFFF8FAFF)
    val BgMidLight    = Color(0xFFF3F6FD)
    val BgBottomLight = Color(0xFFEEF3FB)

    val SurfaceDark   = Color(0xFF1C1F26)
    val CardBgDark    = Color(0xFF12140F)
    val CardBgLight   = Color.White
    val CellBgDark    = Color(0xFF16180F)
    val CellBgLight   = Color(0xFFF3F6FB)

    val TextWhite     = Color(0xFFF8F8F8)
    val TextGray      = Color(0xFF9BA1A6)
    val TextPrimaryLight   = Color(0xFF0F172A)
    val TextSecondaryLight = Color(0xFF475569)
    val TextSecondaryDark  = Color(0xFFB8B09A)

    val GoldPremium   = Color(0xFFEAA93A)
    val GoldSoft      = Color(0xFFC4922E)
    val GoldMuted     = Color(0xFF8C6A24)

    val DangerRed     = Color(0xFFEF4444)
    val WarnOrange    = Color(0xFFF59E0B)
    val SuccessGreen  = Color(0xFF22C55E)

    val Blue          = Color(0xFF3B82F6)
    val Purple        = Color(0xFF8B5CF6)
    val Cyan          = Color(0xFF06B6D4)
    val Pink          = Color(0xFFEC4899)

    val BorderDark1   = Color(0xFF3A2F16)
    val BorderDark2   = Color(0xFF5A4318)
    val BorderDark3   = Color(0xFF2A2414)
    val BorderLight1  = Color(0xFFDCE6F6)
    val BorderLight2  = Color(0xFFE9DDF9)
    val BorderLight3  = Color(0xFFD6ECF3)

    val TrackDark     = Color.White.copy(alpha = 0.08f)
    val TrackLight    = Color(0xFFE2E8F0)
}

private object Anim {
    const val STAGGER_MS        = 70L
    const val FADE_DURATION_MS  = 500
    const val DONUT_DURATION_MS = 1400
    const val TAB_FADE_MS       = 300
}

private object Layout {
    const val MAX_MISSING_COLS  = 8
    const val CATEGORICAL_BASE  = 50
    val DonutSize               = 168.dp
}

// ─────────────────────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBack: () -> Unit,
    onExportClick: (String, String) -> Unit
) {
    val uiData by viewModel.uiData.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.BgDark)) {

        Canvas(modifier = Modifier.fillMaxSize().alpha(0.15f)) {
            drawCircle(
                Brush.radialGradient(listOf(AppColors.GoldPremium, Color.Transparent)),
                radius = 500.dp.toPx(),
                center = Offset(size.width * 0.8f, 0f)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ResultHeader(
                title    = uiData.title,
                onBack   = onBack,
                onExport = { onExportClick(uiData.rawJson, uiData.title) }
            )

            if (uiData.parseError != null) {
                ParseErrorBanner(message = uiData.parseError!!)
            }

            CustomTabRow(selectedTab) { selectedTab = it }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(Anim.TAB_FADE_MS)) togetherWith fadeOut(tween(Anim.TAB_FADE_MS))
                },
                label = "tab_switch"
            ) { target ->
                when (target) {
                    0 -> OverviewDashboard(uiData)
                    1 -> RealChartsTab(uiData)
                    2 -> RealExplorerTab(uiData, viewModel)
                    3 -> ExportTab(uiData.title) { onExportClick(uiData.rawJson, uiData.title) }
                }
            }
        }
    }
}

@Composable
fun ResultHeader(title: String, onBack: () -> Unit, onExport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(AppColors.SurfaceDark, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.TextWhite)
        }

        Text(
            title,
            color      = AppColors.TextWhite,
            fontSize   = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier   = Modifier.weight(1f).padding(horizontal = 8.dp),
            maxLines   = 1
        )

        IconButton(
            onClick = onExport,
            modifier = Modifier.background(AppColors.GoldPremium.copy(0.15f), CircleShape)
        ) {
            Icon(Icons.Default.Download, "Export", tint = AppColors.GoldPremium)
        }
    }
}

@Composable
private fun ParseErrorBanner(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DangerRed.copy(alpha = 0.15f))
            .border(1.dp, AppColors.DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(message, color = AppColors.DangerRed, fontSize = 12.sp)
    }
}

@Composable
fun CustomTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Overview", "Charts", "Explorer", "Export")
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor   = Color.Transparent,
        edgePadding      = 20.dp,
        divider          = {},
        indicator        = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(3.dp)
                    .padding(horizontal = 12.dp)
                    .background(
                        AppColors.GoldPremium,
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        color      = if (selectedTab == index) AppColors.GoldPremium else AppColors.TextGray,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = 14.sp
                    )
                }
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// OVERVIEW TAB
// ═════════════════════════════════════════════════════════════
@Composable
fun OverviewDashboard(uiData: ResultUiData) {
    // Always gold/black — matches the rest of the app
    val theme = rememberDashboardTheme(isDark = true)

    val screenWidth   = LocalConfiguration.current.screenWidthDp
    val metricColumns = if (screenWidth >= 840) 3 else 2

    val scoreColor = uiData.dataCompletenessScore.toScoreColor()
    val metrics    = uiData.buildMetrics()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.bgGradient)),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StaggeredItem(0) {
                HeroCard(uiData = uiData, scoreColor = scoreColor, screenWidth = screenWidth)
            }
        }

        item {
            StaggeredItem(1) {
                SectionHeader(
                    title    = "Overview Metrics",
                    subtitle = "Quick operational snapshot of your dataset",
                    theme    = theme
                )
            }
        }

        item {
            StaggeredItem(2) {
                MetricsGrid(metrics = metrics, columns = metricColumns, theme = theme)
            }
        }

        if (uiData.missingByColumn.isNotEmpty()) {
            item {
                StaggeredItem(3) {
                    MissingValuesSection(uiData = uiData, theme = theme)
                }
            }
        }

        if (uiData.detailedNumericStats.isNotEmpty()) {
            item {
                StaggeredItem(4) {
                    SectionHeader(
                        title    = "Numeric Column Statistics",
                        subtitle = "Distribution summary for numeric columns",
                        theme    = theme
                    )
                }
            }
            itemsIndexed(uiData.detailedNumericStats) { index, row ->
                StaggeredItem(5 + index) {
                    NumericStatCard(row = row, theme = theme)
                }
            }
        }

        if (uiData.categoricalStats.isNotEmpty()) {
            item {
                StaggeredItem(Layout.CATEGORICAL_BASE) {
                    SectionHeader(
                        title    = "Categorical Columns",
                        subtitle = "Most common values and uniqueness",
                        theme    = theme
                    )
                }
            }
            itemsIndexed(uiData.categoricalStats) { index, row ->
                StaggeredItem(Layout.CATEGORICAL_BASE + 1 + index) {
                    CategoricalStatCard(row = row, theme = theme)
                }
            }
        }

        item { Spacer(Modifier.height(28.dp)) }
    }
}

@Composable
private fun HeroCard(
    uiData: ResultUiData,
    scoreColor: Color,
    screenWidth: Int
) {
    val compact = screenWidth < 700

    GradientBorderCard(
        borderColors = listOf(
            AppColors.GoldPremium.copy(alpha = 0.55f),
            AppColors.GoldMuted.copy(alpha = 0.35f),
            AppColors.BorderDark3
        ),
        corner = 28.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A1408),
                            Color(0xFF0C0D11),
                            Color(0xFF161208)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    HeroTextBlock(uiData = uiData, scoreColor = scoreColor)
                    DashboardDonutCard(
                        score          = uiData.dataCompletenessScore.toFloat(),
                        missingPercent = uiData.missingValuesPercentage.toFloat(),
                        duplicates     = uiData.duplicateRows,
                        scoreColor     = scoreColor
                    )
                    HeroPillsRow(uiData)
                }
            } else {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(
                        modifier            = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeroTextBlock(uiData = uiData, scoreColor = scoreColor)
                        HeroPillsRow(uiData)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DashboardDonutCard(
                            score          = uiData.dataCompletenessScore.toFloat(),
                            missingPercent = uiData.missingValuesPercentage.toFloat(),
                            duplicates     = uiData.duplicateRows,
                            scoreColor     = scoreColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroTextBlock(uiData: ResultUiData, scoreColor: Color) {
    val scoreLabel = when {
        uiData.dataCompletenessScore >= 85 -> "Excellent quality"
        uiData.dataCompletenessScore >= 60 -> "Good quality"
        uiData.dataCompletenessScore >= 45 -> "Needs attention"
        else                               -> "Critical quality issues"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GlassPill("AI Data Snapshot")

        Text(
            text       = "Dataset health at a glance",
            style      = MaterialTheme.typography.headlineMedium,
            color      = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text  = "Completeness, structure, duplicates, and core statistics — in one view.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.78f)
        )

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(scoreColor)
            )
            Text(
                text       = scoreLabel,
                color      = Color.White,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HeroPillsRow(uiData: ResultUiData) {
    Row(
        modifier              = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassPill("${uiData.totalRecords.fmtInt()} rows")
        GlassPill("${uiData.totalColumns.fmtInt()} columns")
        GlassPill("${uiData.numericColumnsCount.fmtInt()} numeric")
        GlassPill("${uiData.categoricalColumnsCount.fmtInt()} text")
        GlassPill("${uiData.missingValuesPercentage.fmt(1)}% missing")
    }
}

@Composable
private fun MetricsGrid(
    metrics: List<DashboardMetricUi>,
    columns: Int,
    theme: DashboardTheme
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.chunked(columns).forEach { rowItems ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { metric ->
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        metric   = metric,
                        theme    = theme
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MissingValuesSection(uiData: ResultUiData, theme: DashboardTheme) {
    SectionCard(
        title    = "Missing Values by Column",
        subtitle = "Columns with highest null percentages",
        theme    = theme
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            uiData.missingByColumn.entries
                .sortedByDescending { it.value }
                .take(Layout.MAX_MISSING_COLS)
                .forEach { (column, percent) ->
                    MissingColumnRow(
                        column        = column,
                        percent       = percent,
                        track         = theme.track,
                        textPrimary   = theme.textPrimary,
                        textSecondary = theme.textSecondary
                    )
                }
        }
    }
}

@Composable
private fun MissingColumnRow(
    column: String,
    percent: Double,
    track: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val barColor = percent.toMissingColor()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text     = column,
                style    = MaterialTheme.typography.titleMedium,
                color    = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = "${percent.fmt(1)}%",
                style      = MaterialTheme.typography.titleMedium,
                color      = barColor,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress   = { (percent.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier   = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(100.dp)),
            color      = barColor,
            trackColor = track
        )
    }
}

@Composable
private fun NumericStatCard(row: ColumnStatRow, theme: DashboardTheme) {
    GradientBorderCard(borderColors = theme.borderColors, corner = 24.dp) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBg)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text       = row.columnName,
                            style      = MaterialTheme.typography.titleLarge,
                            color      = theme.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Distribution summary",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.textSecondary
                        )
                    }
                    ColoredIconBadge(icon = Icons.Default.AutoGraph, tint = AppColors.GoldPremium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCell("Mean",   row.mean.fmt(2),   theme, Modifier.weight(1f))
                        StatCell("Median", row.median.fmt(2), theme, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCell("Min",    row.min.fmt(2),    theme, Modifier.weight(1f))
                        StatCell("Max",    row.max.fmt(2),    theme, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCell("Std Dev", row.std.fmt(2),        theme, Modifier.weight(1f))
                        StatCell("Nulls",   row.nullCount.fmtInt(), theme, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoricalStatCard(row: CategoricalStatRow, theme: DashboardTheme) {
    GradientBorderCard(borderColors = theme.borderColors, corner = 24.dp) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBg)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(
                        modifier            = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text       = row.columnName,
                            style      = MaterialTheme.typography.titleLarge,
                            color      = theme.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Categorical breakdown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.textSecondary
                        )
                    }
                    ColoredIconBadge(icon = Icons.Default.GridView, tint = AppColors.GoldSoft)
                }

                Row(
                    modifier              = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoTag("Unique",    row.uniqueValues.fmtInt(),                          theme)
                    InfoTag("Top",       row.topValues.keys.firstOrNull() ?: "N/A",          theme)
                    InfoTag("Frequency", (row.topValues.values.firstOrNull() ?: 0).fmtInt(), theme)
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// CHARTS TAB
// ═════════════════════════════════════════════════════════════
@Composable
fun RealChartsTab(uiData: ResultUiData) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        ChartSectionLabel("DATASET SUMMARY")
        Spacer(Modifier.height(16.dp))

        DatasetHealthOverviewCard(uiData)

        Spacer(Modifier.height(32.dp))
        ChartSectionLabel("VISUAL ANALYTICS")
        Spacer(Modifier.height(16.dp))

        ChartBlock("Missing Values by Column (%)", uiData.missingByColumn.isNotEmpty()) {
            BarChartCompose(uiData.missingByColumn)
        }

        val uniqueMap = uiData.categoricalStats.associate { it.columnName to it.uniqueValues.toDouble() }
        ChartBlock("Unique Values in Categorical Columns", uniqueMap.isNotEmpty()) {
            BarChartCompose(uniqueMap)
        }

        val outlierMap = uiData.detailedNumericStats
            .filter { (it.outlierCount ?: 0) > 0 }
            .associate { it.columnName to (it.outlierCount ?: 0).toDouble() }
        ChartBlock("Outliers per Numeric Column", outlierMap.isNotEmpty()) {
            BarChartCompose(outlierMap)
        }

        val firstCat = uiData.categoricalStats.firstOrNull()
        if (firstCat != null && firstCat.topValues.isNotEmpty()) {
            ChartBlock("Distribution: ${firstCat.columnName}", true) {
                PieChartCompose(firstCat.topValues)
            }
        }

        val noData = uiData.missingByColumn.isEmpty()
                && uniqueMap.isEmpty()
                && outlierMap.isEmpty()
                && firstCat == null

        if (noData) EmptyChartsHint()

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ChartSectionLabel(text: String) {
    Text(
        text          = text,
        color         = AppColors.GoldPremium,
        fontSize      = 12.sp,
        letterSpacing = 2.sp,
        fontWeight    = FontWeight.Bold
    )
}

@Composable
fun DatasetHealthOverviewCard(uiData: ResultUiData) {
    val healthColor = if (uiData.dataCompletenessScore > 80) AppColors.SuccessGreen else AppColors.WarnOrange

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.SurfaceDark.copy(0.6f))
            .border(1.dp, AppColors.GoldPremium.copy(0.2f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(
                        color      = Color.White.copy(0.1f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        style      = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color      = healthColor,
                        startAngle = -90f,
                        sweepAngle = (uiData.dataCompletenessScore / 100f * 360f).toFloat(),
                        useCenter  = false,
                        style      = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiData.dataCompletenessScore.toInt()}%",
                        color = AppColors.TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black
                    )
                    Text("Health", color = AppColors.TextGray, fontSize = 9.sp)
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HealthStatRow("Total Rows",    uiData.totalRecords.fmtInt())
                HealthStatRow("Total Columns", uiData.totalColumns.toString())
                HealthStatRow(
                    label      = "Duplicates",
                    value      = uiData.duplicateRows.toString(),
                    valueColor = if (uiData.duplicateRows > 0) AppColors.DangerRed else AppColors.TextWhite
                )
                HealthStatRow(
                    label      = "Memory Size",
                    value      = "~${uiData.estimatedMemoryMb.fmt(1)} MB",
                    valueColor = AppColors.GoldPremium
                )
            }
        }
    }
}

@Composable
private fun HealthStatRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.TextWhite
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppColors.TextGray, fontSize = 12.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyChartsHint() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.BarChart, null, tint = AppColors.TextGray, modifier = Modifier.size(60.dp))
            Spacer(Modifier.height(12.dp))
            Text("No chartable data available", color = AppColors.TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ChartBlock(title: String, visible: Boolean, content: @Composable () -> Unit) {
    if (!visible) return
    Column(Modifier.padding(bottom = 24.dp)) {
        Text(title, color = AppColors.TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.SurfaceDark.copy(0.4f))
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(24.dp))
                .padding(8.dp)
        ) { content() }
    }
}

// ═════════════════════════════════════════════════════════════
// EXPLORER TAB
// ═════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealExplorerTab(uiData: ResultUiData, viewModel: ResultViewModel) {
    val numericCols     = uiData.numericColumns
    val categoricalCols = uiData.categoricalStats.map { it.columnName }

    if (numericCols.isEmpty() && categoricalCols.isEmpty()) {
        EmptyExplorerHint("No columns to explore")
        return
    }

    val allColsForX = remember(numericCols, categoricalCols) {
        (numericCols + categoricalCols).distinct()
    }

    var selectedX by remember {
        mutableStateOf(numericCols.firstOrNull() ?: categoricalCols.firstOrNull() ?: "")
    }

    val validYOptions = remember(selectedX, numericCols) {
        numericCols.filter { it != selectedX }
    }
    var selectedY by remember { mutableStateOf(validYOptions.firstOrNull() ?: "") }

    val allTypes = listOf("BoxPlot", "Histogram", "Scatter", "Line", "Pie", "Bar", "Radar", "HeatMap")
    val availableTypes = remember(uiData, numericCols, categoricalCols) {
        allTypes.filter { type ->
            when (type) {
                "Scatter", "Line" -> numericCols.size >= 2
                "HeatMap"         -> uiData.correlation.isNotEmpty()
                "Pie", "Bar"      -> categoricalCols.isNotEmpty() || numericCols.isNotEmpty()
                else              -> numericCols.isNotEmpty()
            }
        }
    }

    var selectedChartType by remember(availableTypes) {
        mutableStateOf(availableTypes.firstOrNull() ?: "BoxPlot")
    }

    LaunchedEffect(selectedX, selectedChartType, numericCols) {
        if (selectedChartType in listOf("Scatter", "Line")) {
            val options = numericCols.filter { it != selectedX }
            if (selectedY == selectedX || selectedY !in options) {
                selectedY = options.firstOrNull() ?: ""
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        ChartSectionLabel("DATA EXPLORER")
        Spacer(Modifier.height(16.dp))

        ExplorerDropdown("Primary Axis (X)", selectedX, allColsForX) { selectedX = it }
        Spacer(Modifier.height(12.dp))

        if (selectedChartType in listOf("Scatter", "Line") && validYOptions.isNotEmpty()) {
            ExplorerDropdown("Secondary Axis (Y)", selectedY, validYOptions) { selectedY = it }
            Spacer(Modifier.height(12.dp))
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = availableTypes) { type: String ->
                FilterChip(
                    selected = selectedChartType == type,
                    onClick  = { selectedChartType = type },
                    label    = { Text(text = type, fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.GoldPremium,
                        selectedLabelColor     = AppColors.BgDark,
                        labelColor             = AppColors.TextGray,
                        containerColor         = AppColors.SurfaceDark
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AppColors.SurfaceDark.copy(0.5f))
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(28.dp))
        ) {
            key(selectedChartType, selectedX, selectedY) {
                ExplorerChart(
                    chartType = selectedChartType,
                    x         = selectedX,
                    y         = selectedY,
                    uiData    = uiData,
                    viewModel = viewModel
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        val statA = viewModel.getColumnStat(selectedX)
        if (statA != null) QuickStatsRow(selectedX, statA)

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ExplorerChart(
    chartType: String,
    x: String,
    y: String,
    uiData: ResultUiData,
    viewModel: ResultViewModel
) {
    val statX    = viewModel.getColumnStat(x)
    val catStatX = viewModel.getCategoricalStat(x)

    when (chartType) {
        "BoxPlot"   -> BoxPlotChartContent(statX, x)
        "Histogram" -> HistogramChartContent(statX, x)
        "Scatter"   -> ScatterChartContent(x, y, uiData, viewModel)
        "Line"      -> LineChartContent(x, y, statX, viewModel)
        "Pie"       -> PieChartContent(x, catStatX, statX)
        "Bar"       -> BarChartContent(x, catStatX, statX)
        "Radar"     -> RadarChartContent(x, statX)
        "HeatMap"   -> HeatMapChartContent(uiData)
    }
}

@Composable
private fun BoxPlotChartContent(statX: ColumnStatRow?, x: String) {
    if (statX != null && statX.min != null && statX.max != null) {
        val boxData = BoxPlotData(
            min      = statX.min.toFloat(),
            q1       = (statX.q1 ?: statX.min).toFloat(),
            median   = (statX.median ?: statX.mean ?: statX.min).toFloat(),
            q3       = (statX.q3 ?: statX.max).toFloat(),
            max      = statX.max.toFloat(),
            outliers = statX.outliers,
            label    = x
        )
        ProfessionalBoxPlotCompose(boxData)
    } else {
        EmptyExplorerHint("Numeric stats needed for '$x'")
    }
}

@Composable
private fun HistogramChartContent(statX: ColumnStatRow?, x: String) {
    if (statX != null) {
        HistogramCompose(stat = statX, columnName = x, bins = statX.histogramBins)
    } else {
        EmptyExplorerHint("Numeric column needed")
    }
}

@Composable
private fun ScatterChartContent(
    x: String,
    y: String,
    uiData: ResultUiData,
    viewModel: ResultViewModel
) {
    val points = viewModel.getScatterPoints(x, y)
    if (points.size >= 2) {
        ScatterChartCompose(points, "$x vs $y")
    } else {
        val msg = if (uiData.numericColumns.size < 2)
            "Need at least 2 numeric columns for Scatter"
        else
            "Could not build scatter for $x vs $y"
        EmptyExplorerHint(msg)
    }
}

@Composable
private fun LineChartContent(
    x: String,
    y: String,
    statX: ColumnStatRow?,
    viewModel: ResultViewModel
) {
    val points = viewModel.getLinePoints(x, y)
    when {
        points.size >= 2 -> LineChartCompose(points, "$x → $y")
        statX?.mean != null -> {
            val fallback = listOf(
                0f to (statX.min?.toFloat()    ?: 0f),
                1f to (statX.q1?.toFloat()     ?: 0f),
                2f to (statX.median?.toFloat() ?: statX.mean.toFloat()),
                3f to (statX.q3?.toFloat()     ?: 0f),
                4f to (statX.max?.toFloat()    ?: 0f)
            )
            LineChartCompose(fallback, "$x Distribution")
        }
        else -> EmptyExplorerHint("No trend data available")
    }
}

@Composable
private fun PieChartContent(
    x: String,
    catStatX: CategoricalStatRow?,
    statX: ColumnStatRow?
) {
    when {
        catStatX != null && catStatX.topValues.isNotEmpty() ->
            EnhancedPieWrapper(catStatX.topValues, catStatX.columnName)

        statX != null -> {
            val map = mapOf(
                "Below Q1"  to ((statX.q1  ?: 0.0) - (statX.min    ?: 0.0)).toInt().coerceAtLeast(1),
                "Q1–Median" to ((statX.median ?: 0.0) - (statX.q1  ?: 0.0)).toInt().coerceAtLeast(1),
                "Median–Q3" to ((statX.q3  ?: 0.0) - (statX.median ?: 0.0)).toInt().coerceAtLeast(1),
                "Above Q3"  to ((statX.max  ?: 0.0) - (statX.q3    ?: 0.0)).toInt().coerceAtLeast(1)
            )
            EnhancedPieWrapper(map, x)
        }
        else -> EmptyExplorerHint("No data available")
    }
}

@Composable
private fun BarChartContent(
    x: String,
    catStatX: CategoricalStatRow?,
    statX: ColumnStatRow?
) {
    when {
        catStatX != null && catStatX.topValues.isNotEmpty() ->
            BarChartCompose(catStatX.topValues.mapValues { it.value.toDouble() })

        statX != null -> {
            val cleanMap = mapOf(
                "Mean"   to statX.mean,
                "Median" to statX.median,
                "Min"    to statX.min,
                "Max"    to statX.max,
                "Q1"     to statX.q1,
                "Q3"     to statX.q3
            ).filterValues { it != null }.mapValues { it.value!! }

            if (cleanMap.isNotEmpty()) BarChartCompose(cleanMap)
            else EmptyExplorerHint("No statistics available for '$x'")
        }
        else -> EmptyExplorerHint("No data to plot")
    }
}

@Composable
private fun RadarChartContent(x: String, statX: ColumnStatRow?) {
    if (statX != null) {
        val map = linkedMapOf(
            "Mean"   to (statX.mean   ?: 0.0),
            "Median" to (statX.median ?: 0.0),
            "Std"    to (statX.std    ?: 0.0),
            "Q1"     to (statX.q1     ?: 0.0),
            "Q3"     to (statX.q3     ?: 0.0)
        )
        RadarChartCompose(map)
    } else {
        EmptyExplorerHint("Numeric stats needed")
    }
}

@Composable
private fun HeatMapChartContent(uiData: ResultUiData) {
    if (uiData.correlation.isNotEmpty()) {
        CorrelationHeatMapCompose(uiData.correlation)
    } else {
        EmptyExplorerHint("Correlation matrix not available")
    }
}

@Composable
private fun QuickStatsRow(name: String, s: ColumnStatRow) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.SurfaceDark.copy(0.4f))
            .border(1.dp, AppColors.GoldPremium.copy(0.15f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(name, color = AppColors.GoldPremium, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniStat("Mean",   s.mean.fmt())
                MiniStat("Median", s.median.fmt())
                MiniStat("Std",    s.std.fmt())
                MiniStat("Range",  "${s.min.fmt()} → ${s.max.fmt()}")
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, color = AppColors.TextGray,  fontSize = 9.sp)
        Text(value, color = AppColors.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyExplorerHint(msg: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, null, tint = AppColors.TextGray, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(msg, color = AppColors.TextGray, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = selected,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label, color = AppColors.GoldPremium, fontSize = 12.sp) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedTextColor     = AppColors.TextWhite,
                unfocusedTextColor   = AppColors.TextWhite,
                focusedBorderColor   = AppColors.GoldPremium,
                unfocusedBorderColor = Color.White.copy(0.1f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            modifier         = Modifier.background(AppColors.SurfaceDark)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(option, color = AppColors.TextWhite) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ExportTab(title: String, onExportClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PictureAsPdf, null, tint = AppColors.GoldPremium, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("Ready to Export", color = AppColors.TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Full statistical report for $title", color = AppColors.TextGray, fontSize = 14.sp)
        Spacer(Modifier.height(40.dp))
        Button(
            onClick  = onExportClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AppColors.GoldPremium),
            shape    = RoundedCornerShape(20.dp)
        ) {
            Text("GENERATE PDF REPORT", color = AppColors.BgDark, fontWeight = FontWeight.Bold)
        }
    }
}

// ═════════════════════════════════════════════════════════════
// SHARED PRIMITIVES
// ═════════════════════════════════════════════════════════════
@Composable
private fun GlassPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(AppColors.GoldPremium.copy(alpha = 0.12f))
            .border(1.dp, AppColors.GoldPremium.copy(alpha = 0.28f), RoundedCornerShape(100.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text       = text,
            color      = Color.White,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GradientBorderCard(
    borderColors: List<Color>,
    corner: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(borderColors), RoundedCornerShape(corner))
            .padding(1.dp)
    ) {
        Box(modifier = Modifier.clip(RoundedCornerShape(corner))) {
            content()
        }
    }
}

@Composable
private fun ColoredIconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    theme: DashboardTheme
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.headlineSmall,
            color      = theme.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = theme.textSecondary
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    theme: DashboardTheme,
    content: @Composable ColumnScope.() -> Unit
) {
    GradientBorderCard(borderColors = theme.borderColors, corner = 24.dp) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(24.dp),
            colors   = CardDefaults.cardColors(containerColor = theme.cardBg)
        ) {
            Column(
                modifier            = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = theme.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.textSecondary
                )
                content()
            }
        }
    }
}

@Composable
private fun StatCell(
    title: String,
    value: String,
    theme: DashboardTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(theme.cellBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = theme.textSecondary)
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            color      = theme.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoTag(
    label: String,
    value: String,
    theme: DashboardTheme
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(theme.cellBg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = theme.textSecondary)
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            color      = theme.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

private data class DashboardMetricUi(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color,
    val hint: String? = null
)

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    metric: DashboardMetricUi,
    theme: DashboardTheme
) {
    GradientBorderCard(
        modifier     = modifier,
        borderColors = theme.borderColors,
        corner       = 22.dp
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(22.dp),
            colors   = CardDefaults.cardColors(containerColor = theme.cardBg)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(metric.tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(metric.icon, contentDescription = metric.title, tint = metric.tint)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = metric.value,
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = theme.textPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text  = metric.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.textSecondary
                    )
                    if (metric.hint != null) {
                        Text(
                            text  = metric.hint,
                            style = MaterialTheme.typography.labelSmall,
                            color = metric.tint.copy(alpha = 0.9f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(metric.tint.copy(alpha = 0.35f))
                )
            }
        }
    }
}

@Composable
private fun DashboardDonutCard(
    score: Float,
    missingPercent: Float,
    duplicates: Int,
    scoreColor: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, AppColors.GoldPremium.copy(alpha = 0.22f))
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedDonutChart(
                percentage = (score / 100f).coerceIn(0f, 1f),
                centerText = "${score.toInt()}%",
                label      = "Completeness",
                accent     = AppColors.GoldPremium
            )

            HorizontalDivider(color = AppColors.GoldPremium.copy(alpha = 0.14f))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DonutMiniStat(
                    title = "Missing",
                    value = "${missingPercent.toDouble().fmt(1)}%",
                    color = missingPercent.toDouble().toMissingColor()
                )
                DonutMiniStat(
                    title = "Duplicates",
                    value = duplicates.fmtInt(),
                    color = if (duplicates > 0) AppColors.DangerRed else AppColors.SuccessGreen
                )
                DonutMiniStat(
                    title = "Health",
                    value = when {
                        score >= 85f -> "Great"
                        score >= 60f -> "Okay"
                        else         -> "Poor"
                    },
                    color = scoreColor
                )
            }
        }
    }
}

@Composable
private fun DonutMiniStat(title: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.72f))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AnimatedDonutChart(
    percentage: Float,
    centerText: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        progress.animateTo(
            targetValue   = percentage,
            animationSpec = tween(Anim.DONUT_DURATION_MS)
        )
    }

    Box(modifier = modifier.size(Layout.DonutSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke   = 16.dp.toPx()
            val diameter = min(size.width, size.height)
            val arcSize  = Size(diameter, diameter)
            val topLeft  = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            drawArc(
                color      = Color.White.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(accent.copy(alpha = 0.45f), accent, Color(0xFFFFF3D0))
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress.value,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = centerText,
                style      = MaterialTheme.typography.headlineMedium,
                color      = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * Anim.STAGGER_MS)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(animationSpec = tween(Anim.FADE_DURATION_MS)) +
                expandVertically(animationSpec = tween(Anim.FADE_DURATION_MS))
    ) {
        content()
    }
}

private data class DashboardTheme(
    val bgGradient    : List<Color>,
    val cardBg        : Color,
    val cellBg        : Color,
    val textPrimary   : Color,
    val textSecondary : Color,
    val borderColors  : List<Color>,
    val track         : Color
)

@Composable
private fun rememberDashboardTheme(isDark: Boolean): DashboardTheme = remember(isDark) {
    if (isDark) DashboardTheme(
        bgGradient    = listOf(AppColors.BgDark, Color(0xFF0C0E0A), Color(0xFF10140C)),
        cardBg        = AppColors.CardBgDark,
        cellBg        = AppColors.CellBgDark,
        textPrimary   = AppColors.TextWhite,
        textSecondary = AppColors.TextSecondaryDark,
        borderColors  = listOf(AppColors.BorderDark1, AppColors.BorderDark2, AppColors.BorderDark3),
        track         = AppColors.TrackDark
    ) else DashboardTheme(
        bgGradient    = listOf(AppColors.BgTopLight, AppColors.BgMidLight, AppColors.BgBottomLight),
        cardBg        = AppColors.CardBgLight,
        cellBg        = AppColors.CellBgLight,
        textPrimary   = AppColors.TextPrimaryLight,
        textSecondary = AppColors.TextSecondaryLight,
        borderColors  = listOf(AppColors.BorderLight1, AppColors.BorderLight2, AppColors.BorderLight3),
        track         = AppColors.TrackLight
    )
}

private fun Double.toScoreColor() = when {
    this >= 85 -> AppColors.SuccessGreen
    this >= 60 -> AppColors.WarnOrange
    else       -> AppColors.DangerRed
}

private fun Double.toMissingColor() = when {
    this > 20 -> AppColors.DangerRed
    this > 5  -> AppColors.WarnOrange
    else      -> AppColors.SuccessGreen
}

private fun ResultUiData.buildMetrics(): List<DashboardMetricUi> {
    val duplicateColor = if (duplicateRows > 0) AppColors.DangerRed else AppColors.SuccessGreen
    val missingColor   = missingValuesPercentage.toMissingColor()
    val duplicateHint  = when {
        duplicateRows == 0 -> "No duplicate rows"
        duplicateRows == 1 -> "1 duplicate row"
        else               -> "$duplicateRows duplicate rows"
    }

    return listOf(
        DashboardMetricUi("Rows",         totalRecords.fmtInt(),               Icons.Default.ViewList,   AppColors.GoldPremium),
        DashboardMetricUi("Columns",      totalColumns.fmtInt(),               Icons.Default.ViewColumn, AppColors.GoldSoft),
        DashboardMetricUi("Duplicates",   duplicateRows.fmtInt(),              Icons.Default.CopyAll,    duplicateColor, duplicateHint),
        DashboardMetricUi("Missing",      "${missingValuesPercentage.fmt(1)}%", Icons.Default.Warning,    missingColor),
        DashboardMetricUi("Numeric Cols", numericColumnsCount.fmtInt(),        Icons.Default.Numbers,    AppColors.GoldPremium),
        DashboardMetricUi("Text Cols",    categoricalColumnsCount.fmtInt(),    Icons.Default.Abc,        AppColors.GoldMuted)
    )
}

private fun Int.fmtInt() = this.toString()
private fun Double?.fmt(d: Int = 2) = this?.let { "%.${d}f".format(it) } ?: "—"