package com.example.csvanalyzer.model

import com.google.gson.annotations.SerializedName

// النتيجة الأساسية لبيانات GoBike الراجع من endpoint الـ /stats
data class StatsResult(
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("total_columns") val totalColumns: Int,
    @SerializedName("duplicate_rows") val duplicateRows: Int,
    @SerializedName("total_missing_values") val totalMissingValues: Int? = null,
    @SerializedName("missing_values_percentage") val missingValuesPercentage: Double,
    @SerializedName("numeric_summary") val numericSummary: Map<String, NumericStats>?,
    @SerializedName("categorical_summary") val categoricalSummary: Map<String, CategoricalStats>?
)