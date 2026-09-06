package com.example.csvanalyzer.model

import com.google.gson.annotations.SerializedName

data class AnalysisResult(
    @SerializedName("total_rows")
    val totalRecords: Int? = null,

    @SerializedName("total_columns")
    val totalColumns: Int? = null,

    @SerializedName("columns")
    val columns: List<String>? = null,

    @SerializedName("duplicates")
    val duplicateRows: Int? = null,

    @SerializedName("missing_values")
    val missingValues: Map<String, Int>? = null,

    @SerializedName("missing_pct")
    val missingValuesPercentage: Double? = null,

    @SerializedName("total_missing_values")
    val totalMissingValues: Int? = null,

    @SerializedName("numeric_cols")
    val numericCols: List<String>? = null,

    @SerializedName("numeric_summary")
    val numericSummary: Map<String, NumericStats>? = null,

    @SerializedName("categorical_cols")
    val categoricalCols: List<String>? = null,

    @SerializedName("cat_summary")
    val categoricalSummary: Map<String, CategoricalStats>? = null,

    @SerializedName("correlation")
    val correlation: Map<String, Map<String, Double>>? = null,

    @SerializedName("scatter_pairs")
    val scatterPairs: List<ScatterPair>? = null,

    @SerializedName("data_types")
    val dataTypes: Map<String, String>? = null,

    val fileName: String? = null
)

data class NumericStats(
    @SerializedName("mean")
    val mean: Double? = null,

    @SerializedName("median")
    val median: Double? = null,

    @SerializedName("mode")
    val mode: String? = null,

    @SerializedName("std")
    val std: Double? = null,

    @SerializedName("variance")
    val variance: Double? = null,

    @SerializedName("min")
    val min: Double? = null,

    @SerializedName("max")
    val max: Double? = null,

    @SerializedName("q1")
    val q1: Double? = null,

    @SerializedName("q3")
    val q3: Double? = null,

    @SerializedName("iqr")
    val iqr: Double? = null,

    @SerializedName("skewness")
    val skewness: Double? = null,

    @SerializedName("kurtosis")
    val kurtosis: Double? = null,

    @SerializedName("outlier_count")
    val outlierCount: Int? = null,

    @SerializedName("outlier_pct")
    val outlierPct: Double? = null,

    @SerializedName("null_count")
    val nullCount: Int? = null,

    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("scatter_data")
    val scatterData: List<ScatterPoint>? = null,

    @SerializedName("outlier_points")
    val outlierPoints: List<ScatterPoint>? = null,

    @SerializedName("sample_values")
    val sampleValues: List<Double>? = null,

    @SerializedName("histogram_bins")
    val histogramBins: List<HistogramBin>? = null
)

data class HistogramBin(
    @SerializedName("bin_start") val binStart: Double?,
    @SerializedName("bin_end")   val binEnd: Double?,
    @SerializedName("count")     val count: Int?,
    @SerializedName("label")     val label: String?
)

data class CategoricalStats(
    @SerializedName("unique_values")
    val uniqueValues: Int? = null,

    @SerializedName("null_count")
    val nullCount: Int? = null,

    @SerializedName("top_values")
    val topValues: Map<String, Int>? = null
)

data class ScatterPoint(
    @SerializedName("x")
    val x: Double? = null,

    @SerializedName("y")
    val y: Double? = null
)

data class ScatterPair(
    @SerializedName("x_col")
    val xCol: String? = null,

    @SerializedName("y_col")
    val yCol: String? = null,

    @SerializedName("points")
    val points: List<ScatterPoint>? = null
)
