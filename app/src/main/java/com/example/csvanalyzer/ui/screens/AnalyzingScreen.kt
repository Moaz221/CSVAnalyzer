package com.example.csvanalyzer.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AnalyzingScreen(onBack: () -> Unit, onAnalysisComplete: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "data_flow")

    // أنيميشن لليزر المسح (Scanning Laser)
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "laser"
    )

    // أنيميشن لدوران الحلقات (Orbital Rotation)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotation"
    )

    // أنيميشن لأعمدة البيانات (Equalizer)
    val barScales = List(15) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(Random.nextInt(400, 800), easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = ""
        )
    }

    LaunchedEffect(Unit) {
        delay(5000) // وقت التحليل
        onAnalysisComplete()
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {

        // 1. خلفية الشبكة الرقمية (Digital Grid)
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.15f)) {
            val step = 40.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                drawLine(Color.Gray, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 0.5.dp.toPx())
            }
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawLine(Color.Gray, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 0.5.dp.toPx())
            }
        }

        // 2. ليزر المسح (Scanning Laser Line)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.02f)
                .offset(y = 800.dp * scanY) // يتحرك مع الـ scanY
                .background(Brush.verticalGradient(listOf(Color.Transparent, GoldPremium, Color.Transparent)))
                .alpha(0.6f)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SYSTEM ANALYSIS IN PROGRESS",
                color = GoldPremium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 3. النواة المركزية (Neural Core)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    // حلقة خارجية منقطة
                    drawArc(
                        color = GoldPremium,
                        startAngle = rotation,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )
                    // حلقة داخلية سريعة
                    drawArc(
                        color = Color.White,
                        startAngle = -rotation * 2,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PROCESSING", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("DATA", color = GoldPremium, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 4. "Live Feed" نصوص برمجية وهمية بتتحرك
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column {
                    repeat(4) {
                        Text(
                            "HEX_ADDR: 0x${Random.nextInt(1000, 9999)} | ANALYZING_CHUNK_${Random.nextInt(10, 99)}... OK",
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.alpha(scanY) // تختفي وتظهر مع الليزر
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. الـ Visualizer (أعمدة البيانات اللي تحت)
            Text(
                "DATA FREQUENCY MAP",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                barScales.forEach { scale ->
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight(scale.value)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoldPremium, GoldPremium.copy(alpha = 0.3f))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}