package com.example.csvanalyzer.model

/**
 * هذا الموديل يحمل "الحقيقة" التي استخرجها الموبايل محلياً أثناء قراءة الملف.
 */
data class LocalCsvProfile(
    val fileName: String = "",
    val totalRows: Int = 0,
    val totalCols: Int = 0,
    val totalMissingCells: Int = 0,
    val headers: List<String> = emptyList(),
    val numericColumns: List<String> = emptyList(),
    val categoricalColumns: List<String> = emptyList()
)
