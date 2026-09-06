package com.example.csvanalyzer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.csvanalyzer.R
import com.example.csvanalyzer.ui.components.PremiumBottomNavigation
import com.example.csvanalyzer.ui.components.PremiumTopBar
import com.example.csvanalyzer.ui.components.dashedBorder
import com.example.csvanalyzer.ui.theme.BgDark
import com.example.csvanalyzer.ui.theme.GoldPremium
import com.example.csvanalyzer.ui.theme.IconGreen
import com.example.csvanalyzer.ui.theme.IconGreenBg
import com.example.csvanalyzer.ui.theme.MainFont
import com.example.csvanalyzer.ui.theme.SurfaceDark
import com.example.csvanalyzer.ui.theme.TextGray
import com.example.csvanalyzer.ui.theme.TextWhite
import com.example.csvanalyzer.viewmodel.MainUiState
import com.example.csvanalyzer.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // مش لازم نوقف التطبيق لو الصلاحية الدائمة فشلت
        }

        val fileName = context.getFileNameFromUri(uri)
        selectedFileName = fileName

        if (!context.isProbablyCsv(uri)) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Please select a valid CSV file.",
                    withDismissAction = true
                )
            }
            return@rememberLauncherForActivityResult
        }

        viewModel.uploadAndAnalyzeCsv(context, uri)
    }

    val isLoading = uiState is MainUiState.Loading

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is MainUiState.Error) {
            snackbarHostState.showSnackbar(
                message = state.message,
                withDismissAction = true
            )
        }
    }

    Scaffold(
        topBar = { PremiumTopBar() },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            HomeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                HeroSection()

                Spacer(modifier = Modifier.height(28.dp))

                FuturisticUploadCard(
                    selectedFileName = selectedFileName,
                    enabled = !isLoading,
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv",
                                "application/vnd.ms-excel",
                                "text/plain",
                                "application/octet-stream"
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                SectionHeader(
                    title = "Try Demo",
                    action = "View All"
                )

                Spacer(modifier = Modifier.height(14.dp))

                DemoDatasetCard(
                    enabled = !isLoading,
                    onClick = { viewModel.fetchGoBikeDemo() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                FeatureMiniCards()
            }

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.96f),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.96f)
            ) {
                LoadingOverlay(
                    message = (uiState as? MainUiState.Loading)?.message ?: "Analyzing..."
                )
            }
        }
    }
}

/* ============================================================
   Background
   ============================================================ */

@Composable
private fun HomeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "home_bg")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.back_home),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
                .scale(glowScale)
                .blur(70.dp)
                .background(GoldPremium.copy(alpha = 0.16f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 40.dp)
                .scale(glowScale)
                .blur(60.dp)
                .background(Color(0xFF4CAF50).copy(alpha = 0.11f), CircleShape)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.12f)
        ) {
            val step = 42.dp.toPx()

            var x = 0f
            while (x <= size.width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.13f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.6.dp.toPx()
                )
                x += step
            }

            var y = 0f
            while (y <= size.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.13f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.6.dp.toPx()
                )
                y += step
            }
        }
    }
}

/* ============================================================
   Hero
   ============================================================ */

@Composable
private fun HeroSection() {
    Column {
        Text(
            text = "Analyze Your CSV\nData in Seconds",
            color = TextWhite,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 40.sp,
            fontFamily = MainFont
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Upload your CSV file and unlock instant statistics,\nvisual charts, data quality insights, and reports.",
            color = TextWhite.copy(alpha = 0.7f),
            fontSize = 14.sp,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumChip("Fast")
            PremiumChip("Smart")
            PremiumChip("Visual")
        }
    }
}

@Composable
private fun PremiumChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(GoldPremium.copy(alpha = 0.13f))
            .border(
                width = 1.dp,
                color = GoldPremium.copy(alpha = 0.28f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = GoldPremium,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ============================================================
   Upload Card
   ============================================================ */

@Composable
private fun FuturisticUploadCard(
    selectedFileName: String?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "upload_card")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF171717).copy(alpha = 0.88f),
                        Color(0xFF0B0B0B).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldPremium.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.08f),
                        GoldPremium.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .dashedBorder(
                color = GoldPremium.copy(alpha = 0.35f * pulse),
                shapeRadius = 28.dp
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldPremium.copy(alpha = 0.18f * pulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.55f,
                center = center
            )

            drawArc(
                color = GoldPremium.copy(alpha = 0.22f),
                startAngle = ringRotation,
                sweepAngle = 110f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(0.96f + (pulse * 0.06f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(GoldPremium.copy(alpha = 0.16f))
                    .border(
                        width = 1.dp,
                        color = GoldPremium.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CSV",
                    color = GoldPremium,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = selectedFileName ?: "Drop your CSV file here",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "or choose a file from your device",
                color = TextWhite.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPremium,
                    disabledContainerColor = GoldPremium.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 34.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = if (enabled) "Choose File" else "Processing...",
                    color = BgDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CSV only • Recommended max size: 50MB",
                color = GoldPremium.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ============================================================
   Demo Card
   ============================================================ */

@Composable
private fun SectionHeader(
    title: String,
    action: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = action,
            color = GoldPremium,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DemoDatasetCard(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.82f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IconGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = "CSV",
                tint = IconGreen,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "gobike_dataset.csv",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "18.4 MB • Pre-loaded Data",
                color = TextWhite.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GoldPremium.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start demo",
                tint = GoldPremium,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = TextGray
        )
    }
}

/* ============================================================
   Feature Mini Cards
   ============================================================ */

@Composable
private fun FeatureMiniCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniFeatureCard(
            title = "Stats",
            subtitle = "Mean, Median",
            modifier = Modifier.weight(1f)
        )

        MiniFeatureCard(
            title = "Charts",
            subtitle = "Visual insights",
            modifier = Modifier.weight(1f)
        )

        MiniFeatureCard(
            title = "Export",
            subtitle = "PDF report",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniFeatureCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.66f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = GoldPremium,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = TextWhite.copy(alpha = 0.6f),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

/* ============================================================
   Loading Overlay
   ============================================================ */

@Composable
private fun LoadingOverlay(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_overlay")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(pulse)
                .blur(45.dp)
                .background(GoldPremium.copy(alpha = 0.16f), CircleShape)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = GoldPremium,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = message,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Preparing analysis engine...",
                color = TextWhite.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

/* ============================================================
   File Helpers
   ============================================================ */

private fun Context.getFileNameFromUri(uri: Uri): String {
    return try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                uri.lastPathSegment ?: "selected_file.csv"
            }
        } ?: (uri.lastPathSegment ?: "selected_file.csv")
    } catch (_: Exception) {
        uri.lastPathSegment ?: "selected_file.csv"
    }
}

private fun Context.isProbablyCsv(uri: Uri): Boolean {
    val fileName = getFileNameFromUri(uri).lowercase()
    val mimeType = contentResolver.getType(uri)?.lowercase().orEmpty()

    return fileName.endsWith(".csv") ||
            mimeType == "text/csv" ||
            mimeType == "text/comma-separated-values" ||
            mimeType == "application/csv" ||
            mimeType == "application/vnd.ms-excel" ||
            mimeType == "text/plain"
}
