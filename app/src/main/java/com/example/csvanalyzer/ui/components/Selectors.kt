package com.example.csvanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csvanalyzer.ui.theme.*

/**
 * Multi-select لاختيار عدة أعمدة بستايل Premium
 */
@Composable
fun MultiColumnSelector(
    label: String,
    columns: List<String>,
    selectedColumns: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    maxSelection: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "$label (${selectedColumns.size} selected)",
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
                    text = if (selectedColumns.isEmpty()) "Select columns..."
                    else selectedColumns.joinToString(", ").take(30) +
                            if (selectedColumns.joinToString(", ").length > 30) "..." else "",
                    color = if (selectedColumns.isNotEmpty()) TextWhite else TextGray,
                    fontSize = 14.sp
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = GoldPremium)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(SurfaceDark)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .heightIn(max = 300.dp)
            ) {
                columns.forEach { column ->
                    val isSelected = column in selectedColumns
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GoldPremium,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Text(
                                    column,
                                    color = if (isSelected) GoldPremium else TextWhite,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        onClick = {
                            val newSelection = if (isSelected) {
                                selectedColumns - column
                            } else {
                                if (selectedColumns.size < maxSelection) {
                                    selectedColumns + column
                                } else selectedColumns
                            }
                            onSelectionChanged(newSelection)
                        }
                    )
                }
            }
        }
    }
}
