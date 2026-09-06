import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val properties = Properties()
val propertiesFile = rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

android {
    namespace = "com.example.csvanalyzer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.csvanalyzer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = properties.getProperty("API_KEY") ?: ""
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // تفعيل ViewBinding للتعامل مع شاشات الـ XML + Compose موجود
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // AndroidX & Material Design الأساسية للواجهات
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Coroutines & Lifecycle (لإدارة العمليات في الخلفية)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Retrofit 2 & Gson (للاتصال بـ API وتحويل البيانات)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp 3 & Logging (لمتابعة الـ Network Requests)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // MPAndroidChart (لرسم الرسوم البيانية Bar و Pie)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // iTextG (مكتبة إنشاء الـ PDF المخصصة للأندرويد)
    implementation("com.itextpdf:itextg:5.5.10")

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // مكتبات الاختبار
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}