package com.example.csvanalyzer.viewmodel

import androidx.lifecycle.ViewModel
import com.example.csvanalyzer.api.SafeGson
import com.example.csvanalyzer.model.AnalysisResult
import com.example.csvanalyzer.model.HistogramBin
import com.example.csvanalyzer.model.LocalCsvProfile
import com.example.csvanalyzer.model.ScatterPair
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class ColumnStatRow(
    val columnName: String,
    val mean: Double?,
    val median: Double?,
    val mode: String?,
    val std: Double?,
    val variance: Double?,
    val min: Double?,
    val max: Double?,
    val range: Double?,
    val q1: Double?,
    val q3: Double?,
    val iqr: Double?,
    val skewness: Double?,
    val kurtosis: Double?,
    val outlierCount: Int?,
    val outlierPct: Double?,
    val outliers: List<Float> = emptyList(),
    val nullCount: Int?,
    val count: Int?,
    val histogramBins: List<com.example.csvanalyzer.model.HistogramBin> = emptyList(),
    // ✅ مهم للـ Scatter fallback
    val sampleValues: List<Float> = emptyList(),
    val scatterYValues: List<Float> = emptyList()
)

data class CategoricalStatRow(
    val columnName: String,
    val uniqueValues: Int,
    val nullCount: Int,
    val topValues: Map<String, Int>
)

data class ResultUiData(
    val title: String = "Analysis Summary",
    val totalRecords: Int = 0,
    val totalColumns: Int = 0,
    val duplicateRows: Int = 0,
    val missingValuesPercentage: Double = 0.0,
    val totalMissingCells: Int = 0,
    val dataCompletenessScore: Double = 100.0,
    val numericColumnsCount: Int = 0,
    val categoricalColumnsCount: Int = 0,
    val estimatedMemoryMb: Double = 0.0,

    val missingByColumn: Map<String, Double> = emptyMap(),
    val categoricalStats: List<CategoricalStatRow> = emptyList(),
    val detailedNumericStats: List<ColumnStatRow> = emptyList(),
    val numericColumns: List<String> = emptyList(),

    val correlation: Map<String, Map<String, Double>> = emptyMap(),
    val scatterPairs: List<ScatterPair> = emptyList(),

    val rawNumericSamples: Map<String, List<Double>> = emptyMap(),

    val rawJson: String = "",
    val parseError: String? = null
)

class ResultViewModel : ViewModel() {

    private val _uiData = MutableStateFlow(ResultUiData())
    val uiData: StateFlow<ResultUiData> = _uiData.asStateFlow()

    fun parseResultJson(json: String?, isDemo: Boolean, localProfile: LocalCsvProfile? = null) {
        if (json.isNullOrEmpty()) {
            _uiData.value = ResultUiData(parseError = "Empty response from server")
            return
        }

        try {
            val parsed = SafeGson.instance.fromJson(json, AnalysisResult::class.java)

            val totalRowsSafe = parsed.totalRecords?.takeIf { it > 0 } ?: localProfile?.totalRows ?: 0
            val totalColsSafe = parsed.totalColumns?.takeIf { it > 0 } ?: localProfile?.totalCols ?: 0
            val rowsForDivision = totalRowsSafe.coerceAtLeast(1)

            val detailedRows = mutableListOf<ColumnStatRow>()
            val categoricalRows = mutableListOf<CategoricalStatRow>()
            val missingMap = linkedMapOf<String, Double>()

            parsed.numericSummary?.forEach { (col, s) ->
                val range = if (s.max != null && s.min != null) s.max - s.min else null
                val iqr = s.iqr ?: if (s.q1 != null && s.q3 != null) s.q3 - s.q1 else null
                val variance = s.variance ?: s.std?.let { it * it }

                // ✅ تصحيح: الـ Median يأخذ قيمته الحقيقية فقط بدون إجبار قراءة الـ Mean
                val safeMedian = s.median

                val nullCount = s.nullCount ?: 0
                if (nullCount > 0) missingMap[col] = (nullCount.toDouble() / rowsForDivision) * 100.0

                detailedRows.add(
                    ColumnStatRow(
                        columnName   = col,
                        mean         = s.mean,
                        median       = safeMedian,
                        mode         = s.mode,
                        std          = s.std,
                        variance     = variance,
                        min          = s.min,
                        max          = s.max,
                        range        = range,
                        q1           = s.q1,
                        q3           = s.q3,
                        iqr          = iqr,
                        skewness     = s.skewness,
                        kurtosis     = s.kurtosis,
                        outlierCount = s.outlierCount,
                        outlierPct   = s.outlierPct,
                        outliers     = s.outlierPoints?.mapNotNull { it.y?.toFloat() } ?: emptyList(),
                        nullCount    = nullCount,
                        count        = s.count,
                        histogramBins = s.histogramBins ?: emptyList(),
                        sampleValues = s.sampleValues?.mapNotNull { it.toFloat() } ?: emptyList(),
                        scatterYValues = s.scatterData?.mapNotNull { it.y?.toFloat() } ?: emptyList()
                    )
                )
            }

            // ✅ قراءة البيانات النصية بشكل سليم وإضافة Log للتأكد
            parsed.categoricalSummary?.forEach { (col, s) ->
                val nullCount = s.nullCount ?: 0
                val unique = s.uniqueValues ?: 0
                val topMap = s.topValues ?: emptyMap()

                if (nullCount > 0 && !missingMap.containsKey(col)) {
                    missingMap[col] = (nullCount.toDouble() / rowsForDivision) * 100.0
                }

                android.util.Log.d("CAT_DEBUG", "Column: $col -> Unique: $unique, TopValues: $topMap")

                categoricalRows.add(
                    CategoricalStatRow(
                        columnName = col,
                        uniqueValues = unique,
                        nullCount = nullCount,
                        topValues = topMap
                    )
                )
            }

            parsed.missingValues?.forEach { (col, count) ->
                if (count > 0 && !missingMap.containsKey(col)) missingMap[col] = (count.toDouble() / rowsForDivision) * 100.0
            }

            val totalMissingCells = parsed.totalMissingValues ?: parsed.missingValues?.values?.sum() ?: detailedRows.sumOf { it.nullCount ?: 0 } + categoricalRows.sumOf { it.nullCount }
            val missingPct = parsed.missingValuesPercentage ?: if (totalMissingCells > 0) (totalMissingCells.toDouble() / (totalRowsSafe * totalColsSafe.coerceAtLeast(1))) * 100.0 else 0.0

            val samplesMap = mutableMapOf<String, List<Double>>()
            parsed.numericSummary?.forEach { (col, s) ->
                if (s.sampleValues != null) {
                    samplesMap[col] = s.sampleValues
                }
            }

            _uiData.value = ResultUiData(
                title = if (isDemo) "GoBike Dataset Insights" else (parsed.fileName ?: "Analysis Report"),
                totalRecords = totalRowsSafe,
                totalColumns = totalColsSafe,
                duplicateRows = parsed.duplicateRows ?: 0,
                missingValuesPercentage = missingPct,
                totalMissingCells = totalMissingCells,
                dataCompletenessScore = (100.0 - missingPct).coerceIn(0.0, 100.0),
                numericColumnsCount = detailedRows.size,
                categoricalColumnsCount = categoricalRows.size,
                estimatedMemoryMb = (totalRowsSafe.toLong() * totalColsSafe.toLong() * 8.0) / (1024.0 * 1024.0),
                missingByColumn = missingMap,
                categoricalStats = categoricalRows,
                detailedNumericStats = detailedRows.sortedByDescending { it.outlierCount ?: 0 },
                numericColumns = detailedRows.map { it.columnName },
                correlation = parsed.correlation ?: emptyMap(),
                scatterPairs = parsed.scatterPairs ?: emptyList(),
                rawNumericSamples = samplesMap,
                rawJson = json,
                parseError = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiData.value = _uiData.value.copy(title = "Data Error", parseError = "Error: ${e.message}")
        }
    }

    fun getColumnStat(column: String): ColumnStatRow? = _uiData.value.detailedNumericStats.firstOrNull { it.columnName == column }

    fun getCategoricalStat(column: String): CategoricalStatRow? = _uiData.value.categoricalStats.firstOrNull { it.columnName == column }

    fun hasScatterDataFor(x: String, y: String): Boolean {
        return _uiData.value.scatterPairs.any {
            (it.xCol == x && it.yCol == y) || (it.xCol == y && it.yCol == x)
        }
    }

    fun getScatterPoints(x: String, y: String): List<Pair<Float, Float>> {
        if (x.isBlank() || y.isBlank() || x == y) return emptyList()

        val allPoints = mutableListOf<Pair<Float, Float>>()

        // ═══════════════════════════════════════
        // SOURCE 1: scatter_pairs من السيرفر (أولاً)
        // ═══════════════════════════════════════
        val pairs = _uiData.value.scatterPairs
        val match = pairs.firstOrNull { pair ->
            val px = pair.xCol?.trim().orEmpty()
            val py = pair.yCol?.trim().orEmpty()
            (px.equals(x, true) && py.equals(y, true)) ||
            (px.equals(y, true) && py.equals(x, true))
        }

        if (match != null) {
            val swapped = match.xCol?.trim().orEmpty().equals(y, true)
            match.points?.forEach { p ->
                val a = p.x?.toFloat()
                val b = p.y?.toFloat()
                if (a != null && b != null) {
                    allPoints.add(if (swapped) b to a else a to b)
                }
            }
        }

        // ═══════════════════════════════════════
        // SOURCE 2: sample_values من العمودين (لزيادة عدد النقط)
        // ═══════════════════════════════════════
        val statX = getColumnStat(x)
        val statY = getColumnStat(y)

        if (statX != null && statY != null) {
            val xSamples = statX.sampleValues.ifEmpty { statX.scatterYValues }
            val ySamples = statY.sampleValues.ifEmpty { statY.scatterYValues }

            val minLen = minOf(xSamples.size, ySamples.size)
            for (i in 0 until minLen) {
                allPoints.add(xSamples[i] to ySamples[i])
            }
        }

        // إزالة النقاط المكررة (إن وجدت) لضمان نظافة الرسم
        val finalPoints = allPoints.distinct().filter { it.first.isFinite() && it.second.isFinite() }
        
        android.util.Log.d("SCATTER_DATA", "Total points for $x vs $y: ${finalPoints.size}")
        
        if (finalPoints.size >= 2) return finalPoints

        // ═══════════════════════════════════════
        // SOURCE 3: synthetic fallback
        // ═══════════════════════════════════════
        if (statX != null && statY != null) {
            fun synth(s: ColumnStatRow): List<Float> = listOfNotNull(
                s.min?.toFloat(), s.q1?.toFloat(), s.median?.toFloat() ?: s.mean?.toFloat(), s.q3?.toFloat(), s.max?.toFloat()
            )
            val sx = synth(statX)
            val sy = synth(statY)
            val n = minOf(sx.size, sy.size)
            if (n >= 2) return (0 until n).map { idx -> sx[idx] to sy[idx] }
        }

        return emptyList()
    }

    fun getLinePoints(x: String, y: String): List<Pair<Float, Float>> =
        getScatterPoints(x, y).sortedBy { it.first }

    fun getValidYColumnsForX(xCol: String): List<String> =
        _uiData.value.numericColumns.filter { it != xCol }
}

fun Double?.fmt(digits: Int = 2): String {
    if (this == null || this.isNaN() || this.isInfinite()) return "N/A"
    return when {
        abs(this) >= 1_000_000 -> String.format("%.2fM", this / 1_000_000)
        abs(this) >= 10_000 -> String.format("%.1fK", this / 1_000)
        abs(this) >= 1000 -> String.format("%.2f", this)
        else -> String.format("%.${digits}f", this)
    }
}
fun Int?.fmtInt(): String {
    if (this == null) return "N/A"
    return when {
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 10_000 -> String.format("%.1fK", this / 1_000.0)
        else -> this.toString()
    }
}
fun String?.fmtText(): String = if (this.isNullOrBlank()) "N/A" else this