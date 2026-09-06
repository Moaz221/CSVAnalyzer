package com.example.csvanalyzer.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://gobike-api-production.up.railway.app/"

    // Interceptor لإضافة API Key لكل الطلبات
    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", com.example.csvanalyzer.BuildConfig.API_KEY)
            .build()
        chain.proceed(request)
    }

    // Logging عشان نشوف الـ request والـ response بالكامل في الـ Logcat
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(logging)
        .connectTimeout(120, TimeUnit.SECONDS) // زيادة وقت الاتصال
        .readTimeout(300, TimeUnit.SECONDS)    // زيادة وقت القراءة للملفات الكبيرة
        .writeTimeout(300, TimeUnit.SECONDS)   // زيادة وقت الكتابة (الرفع)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // استخدام SafeGson لضمان عدم توقف التطبيق عند وجود بيانات غير متوقعة
            .addConverterFactory(GsonConverterFactory.create(SafeGson.instance))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
