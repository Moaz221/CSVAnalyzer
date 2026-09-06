package com.example.csvanalyzer.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csvanalyzer.api.RetrofitClient
import com.example.csvanalyzer.api.SafeGson
import com.example.csvanalyzer.model.AnalysisResult
import com.example.csvanalyzer.model.LocalCsvProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class MainUiState {
    object Idle : MainUiState()
    data class Loading(val message: String) : MainUiState()
    data class Success(val isDemo: Boolean) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // متغيرات حفظ البيانات الحالية
    var currentAnalysisResult: AnalysisResult? = null
        private set

    var currentRawJson: String = ""
        private set

    var currentTitle: String = "Analysis Report"
        private set

    // بروفايل البيانات المحلي
    var currentLocalProfile: LocalCsvProfile? = null
        private set

    fun fetchGoBikeDemo() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading("Loading GoBike Dataset Stats...")
            try {
                // Retrofit دلوقتي بيستخدم SafeGson تلقائياً
                val result = RetrofitClient.apiService.getStats()

                currentAnalysisResult = result
                // استخدام SafeGson في الـ serialization كمان لضمان اتساق البيانات
                currentRawJson = SafeGson.instance.toJson(result)
                currentTitle = "GoBike Dataset Analysis"

                _uiState.value = MainUiState.Success(isDemo = true)
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("GOBIKE_ERROR", "Error: ${e.message}", e)
                val message = when {
                    e is com.google.gson.JsonSyntaxException || e is com.google.gson.stream.MalformedJsonException
                    -> "The server returned an invalid response. SafeGson handled the parsing but the data structure might be wrong."
                    
                    e is retrofit2.HttpException -> "Server Error (${e.code()}). The analysis engine might be busy."
                    
                    else -> e.localizedMessage ?: "Failed to fetch GoBike data"
                }
                _uiState.value = MainUiState.Error(message)
            }
        }
    }

    fun uploadAndAnalyzeCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading("Uploading large CSV... please wait")

            try {
                // حد أقصى مقترح عشان الموبايل ميتعلقش
                val maxSizeBytes = 40L * 1024L * 1024L // 40MB
                val fileSize = getFileSize(context, uri)

                if (fileSize > maxSizeBytes) {
                    _uiState.value = MainUiState.Error(
                        "File is too large (${fileSize / (1024 * 1024)} MB). Max allowed is 40 MB."
                    )
                    return@launch
                }

                val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: "upload.csv"

                val safeName = if (displayName.endsWith(".csv", ignoreCase = true)) displayName else "$displayName.csv"
                val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.csv")

                val (filePart, localProfile) = analyzeCsvLocally(context, uri, tempFile, safeName)
                currentLocalProfile = localProfile

                val result = RetrofitClient.apiService.analyzeCsv(filePart)

                // الدمج الذكي للبيانات
                val finalResult = result.copy(
                    totalRecords = if ((result.totalRecords ?: 0) <= 1) localProfile.totalRows else result.totalRecords,
                    totalColumns = if ((result.totalColumns ?: 0) <= 0) localProfile.totalCols else result.totalColumns,
                    totalMissingValues = result.totalMissingValues ?: localProfile.totalMissingCells,
                    fileName = result.fileName ?: safeName
                )

                currentAnalysisResult = finalResult
                // استخدام SafeGson لضمان الحفاظ على تنسيق البيانات
                currentRawJson = SafeGson.instance.toJson(finalResult)
                currentTitle = finalResult.fileName ?: "Uploaded CSV Analysis"

                _uiState.value = MainUiState.Success(isDemo = false)

            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CSV_ERROR", "Error: ${e.message}", e)

                val message = when {
                    e is com.google.gson.JsonSyntaxException || e is com.google.gson.stream.MalformedJsonException
                    -> "Server returned malformed data. SafeGson attempted recovery."

                    e is retrofit2.HttpException -> "Server Error (${e.code()}): ${e.message()}"

                    e.message?.contains("Stream was reset", ignoreCase = true) == true ||
                            e.message?.contains("stream was reset", ignoreCase = true) == true ||
                            e.message?.contains("StreamReset", ignoreCase = true) == true
                    -> "Upload interrupted. Try again on stable Wi-Fi, or use a smaller CSV."

                    e.message?.contains("timeout", ignoreCase = true) == true
                    -> "Upload timed out. File may be too large or connection is slow."

                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true
                    -> "No internet connection."

                    else -> e.localizedMessage ?: "Failed to analyze CSV"
                }

                _uiState.value = MainUiState.Error(message)
            }
        }
    }

    private fun analyzeCsvLocally(
        context: Context,
        uri: Uri,
        tempFile: File,
        safeName: String
    ): Pair<MultipartBody.Part, LocalCsvProfile> {
        var rows = 0
        var cols = 0
        var missingCount = 0
        val headers = mutableListOf<String>()

        try {
            // اكتب الملف كامل في tempFile أولاً
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // احسب الإحصائيات من tempFile
            tempFile.bufferedReader().use { reader ->
                val firstLine = reader.readLine()
                if (firstLine != null) {
                    val sep = if (firstLine.contains(';')) ';' else if (firstLine.contains('\t')) '\t' else ','
                    val headerParts = firstLine.split(sep)
                    cols = headerParts.size
                    headers.addAll(headerParts.map { it.trim() })

                    reader.forEachLine { line ->
                        if (line.isNotBlank()) {
                            rows++
                            val parts = line.split(sep, limit = -1)
                            missingCount += parts.count { it.trim().isEmpty() }
                            if (parts.size < cols) missingCount += (cols - parts.size)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val profile = LocalCsvProfile(
            fileName = safeName,
            totalRows = rows,
            totalCols = cols,
            totalMissingCells = missingCount,
            headers = headers
        )

        val requestBody = tempFile.asRequestBody("text/csv".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", safeName, requestBody)

        return Pair(filePart, profile)
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getLong(sizeIndex)
            }
        }

        return context.contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        } ?: 0L
    }

    fun resetState() {
        _uiState.value = MainUiState.Idle
    }
}
