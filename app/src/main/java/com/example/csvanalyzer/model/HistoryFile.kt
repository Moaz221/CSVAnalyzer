package com.example.csvanalyzer.model

data class HistoryFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileSize: String,
    val analysisDate: Long = System.currentTimeMillis(),
    val rawJson: String
)
