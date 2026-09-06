package com.example.csvanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.csvanalyzer.ui.navigation.Screen
import com.example.csvanalyzer.ui.theme.*

// ===== 1. Dashed Border Modifier =====
fun Modifier.dashedBorder(color: Color, shapeRadius: androidx.compose.ui.unit.Dp) = this.drawBehind {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
    drawRoundRect(color = color, style = stroke, cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius.toPx()))
}

// ===== 2. Premium Top Bar =====
@Composable
fun PremiumTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DataAnalyzer", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ===== 3. Premium Bottom Navigation =====
@Composable
fun PremiumBottomNavigation(navController: NavController? = null) {
    val navBackStackEntry = navController?.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.value?.destination?.route

    Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceDark).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = currentRoute == Screen.Home.route,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController?.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        )
        BottomNavItem(
            icon = Icons.Default.History,
            label = "History",
            isSelected = currentRoute == Screen.History.route,
            onClick = {
                if (currentRoute != Screen.History.route) {
                    navController?.navigate(Screen.History.route)
                }
            }
        )
        BottomNavItem(
            icon = Icons.Default.Assessment,
            label = "Reports",
            isSelected = currentRoute == Screen.Reports.route,
            onClick = {
                if (currentRoute != Screen.Reports.route) {
                    navController?.navigate(Screen.Reports.route)
                }
            }
        )
        BottomNavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = currentRoute == Screen.Settings.route,
            onClick = {
                if (currentRoute != Screen.Settings.route) {
                    navController?.navigate(Screen.Settings.route)
                }
            }
        )
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val color = if (isSelected) GoldPremium else TextGray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ===== 4. Progress Steps Component =====
enum class StepState { DONE, CURRENT, PENDING }

@Composable
fun StepItem(title: String, state: StepState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape)
                .background(if (state == StepState.DONE) GoldPremium else BgDark)
                .border(2.dp, if (state == StepState.PENDING) SurfaceDark else GoldPremium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (state == StepState.DONE) {
                Icon(Icons.Default.Check, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
            } else if (state == StepState.CURRENT) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldPremium))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = if (state == StepState.PENDING) TextGray else TextWhite, fontSize = 16.sp, fontWeight = if (state == StepState.CURRENT) FontWeight.Bold else FontWeight.Normal)
        Spacer(modifier = Modifier.weight(1f))
        if (state == StepState.DONE) {
            Icon(Icons.Default.Check, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}