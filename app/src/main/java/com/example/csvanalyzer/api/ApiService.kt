package com.example.csvanalyzer.api

import com.example.csvanalyzer.model.AnalysisResult
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    /**
     * GET /stats
     * بيجيب إحصائيات GoBike الجاهزة من السيرفر
     */
    @GET("stats")
    suspend fun getStats(): AnalysisResult

    /**
     * POST /analyze
     * بيرفع ملف CSV جديد ويحلله ويرجع النتيجة
     * @param file الجزء الخاص بملف الـ CSV (Multipart)
     */
    @Multipart
    @POST("analyze")
    suspend fun analyzeCsv(
        @Part file: MultipartBody.Part
    ): AnalysisResult
}
