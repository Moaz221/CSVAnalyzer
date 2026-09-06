package com.example.csvanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.theme.*

/**
 * Header الاحترافي فوق الرسم - يعرض الأعمدة المختارة كـ Chips
 */
@Composable
fun ChartInfoBar(
    chips: List<String>,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.5f)) // Glass effect
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPremium.copy(alpha = 0.1f))
                        .border(0.5.dp, GoldPremium.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chip,
                        color = GoldPremium,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // زر الإعدادات المتوهج
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(36.dp)
                .background(GoldPremium, CircleShape)
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * تلميح عند عدم وجود بيانات
 */
@Composable
fun EmptyHint(message: String = "Select data columns to begin analysis") {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // أيقونة متوهجة
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .blur(30.dp)
                    .background(GoldPremium.copy(0.2f), CircleShape)
            )
            Text("📊", fontSize = 50.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * BottomSheet لاختيار الأعمدة بستايل Premium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnPickerSheet(
    title: String,
    fields: List<ColumnField>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark, // لون داكن فخم
        tonalElevation = 8.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = GoldPremium) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                title,
                color = GoldPremium,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            fields.forEach { field ->
                CompactColumnDropdown(
                    label = field.label,
                    columns = field.availableColumns,
                    selected = field.selectedColumn,
                    onSelected = field.onColumnSelected
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("APPLY SETTINGS", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

/**
 * موديل لكل حقل اختيار
 */
data class ColumnField(
    val label: String,
    val availableColumns: List<String>,
    val selectedColumn: String?,
    val onColumnSelected: (String) -> Unit
)

/**
 * Dropdown مضغوط وأنيق بستايل Dark
 */
@Composable
private fun CompactColumnDropdown(
    label: String,
    columns: List<String>,
    selected: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            label,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected ?: "Choose...",
                    color = if (selected != null) TextWhite else TextGray,
                    fontSize = 14.sp,
                    fontWeight = if (selected != null) FontWeight.SemiBold else FontWeight.Normal
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    null,
                    tint = GoldPremium,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(SurfaceDark)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .heightIn(max = 300.dp)
            ) {
                columns.forEach { col ->
                    val isSelected = col == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                col,
                                color = if (isSelected) GoldPremium else TextWhite,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelected(col)
                            expanded = false
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = GoldPremium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}
