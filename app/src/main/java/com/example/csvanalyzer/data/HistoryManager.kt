package com.example.csvanalyzer.data

import android.content.Context
import com.example.csvanalyzer.api.SafeGson
import com.example.csvanalyzer.model.HistoryFile
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

class HistoryManager(private val context: Context) {
    private val fileName = "analysis_history.json"
    private val historyFile = File(context.filesDir, fileName)
    
    private val _history = MutableStateFlow<List<HistoryFile>>(emptyList())
    val history: StateFlow<List<HistoryFile>> = _history

    init {
        loadHistory()
    }

    private fun loadHistory() {
        if (!historyFile.exists()) return
        try {
            val json = historyFile.readText()
            val type = object : TypeToken<List<HistoryFile>>() {}.type
            val list: List<HistoryFile> = SafeGson.instance.fromJson(json, type) ?: emptyList()
            _history.value = list.sortedByDescending { it.analysisDate }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addHistoryItem(item: HistoryFile) = withContext(Dispatchers.IO) {
        val currentList = _history.value.toMutableList()
        currentList.add(0, item)
        // Keep only last 20 items to save space
        val limitedList = currentList.take(20)
        saveHistory(limitedList)
        _history.value = limitedList
    }

    private fun saveHistory(list: List<HistoryFile>) {
        try {
            val json = SafeGson.instance.toJson(list)
            historyFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteHistoryItem(id: String) = withContext(Dispatchers.IO) {
        val newList = _history.value.filter { it.id != id }
        saveHistory(newList)
        _history.value = newList
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        saveHistory(emptyList())
        _history.value = emptyList()
    }
}
