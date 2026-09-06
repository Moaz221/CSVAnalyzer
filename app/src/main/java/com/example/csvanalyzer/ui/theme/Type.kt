package com.example.csvanalyzer.ui.theme

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.R

// إعداد الفونت المخصص
val MainFont = FontFamily(
    Font(R.font.main_font, FontWeight.Normal),
    Font(R.font.main_font, FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    // العناوين تستخدم الفونت المخصص
    displayLarge = TextStyle(fontFamily = MainFont),
    displayMedium = TextStyle(fontFamily = MainFont),
    displaySmall = TextStyle(fontFamily = MainFont),
    headlineLarge = TextStyle(fontFamily = MainFont),
    headlineMedium = TextStyle(fontFamily = MainFont),
    headlineSmall = TextStyle(fontFamily = MainFont),
    titleLarge = TextStyle(
        fontFamily = MainFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(fontFamily = MainFont, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = MainFont, fontWeight = FontWeight.Medium),

    // باقي النصوص تستخدم فونت النظام الافتراضي
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default),
    bodySmall = TextStyle(fontFamily = FontFamily.Default),
    labelLarge = TextStyle(fontFamily = FontFamily.Default),
    labelMedium = TextStyle(fontFamily = FontFamily.Default),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)