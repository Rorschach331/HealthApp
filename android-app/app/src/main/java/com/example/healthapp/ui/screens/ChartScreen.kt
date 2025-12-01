package com.example.healthapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.model.Record
import com.example.healthapp.utils.TimeUtils
import com.example.healthapp.viewmodel.MainViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val users by viewModel.users.collectAsState()
    
    val filterName by viewModel.filterName.collectAsState()
    val filterStart by viewModel.filterStart.collectAsState()
    val filterEnd by viewModel.filterEnd.collectAsState()
    
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    val startDateDialogState = rememberMaterialDialogState()
    val endDateDialogState = rememberMaterialDialogState()
    
    // Sync local filter state with ViewModel
    LaunchedEffect(filterName, filterStart, filterEnd) {
        selectedUser = filterName
        startDate = filterStart
        endDate = filterEnd
    }
    
    // Sort by date ascending for chart
    val sortedRecords = records.sortedBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("趋势分析", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        
        // Filter Toggle Button
        OutlinedButton(
            onClick = { filterExpanded = !filterExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (filterExpanded) "收起筛选" else "展开筛选")
            Spacer(Modifier.weight(1f))
            Icon(
                if (filterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
        
        // Filter Panel
        if (filterExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // User Filter
                    var userExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = userExpanded,
                        onExpandedChange = { userExpanded = !userExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedUser.ifEmpty { "全部" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("姓名筛选") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = userExpanded,
                            onDismissRequest = { userExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("全部") },
                                onClick = {
                                    selectedUser = ""
                                    userExpanded = false
                                }
                            )
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user) },
                                    onClick = {
                                        selectedUser = user
                                        userExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Date Range
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = startDate.ifEmpty { "" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("开始日期") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { startDateDialogState.show() }
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = endDate.ifEmpty { "" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("结束日期") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { endDateDialogState.show() }
                            )
                        }
                    }
                    
                    // Quick Filters
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = LocalDate.now()
                                endDate = now.toString()
                                startDate = now.minusDays(7).toString()
                            },
                            label = { Text("近7天") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = LocalDate.now()
                                endDate = now.toString()
                                startDate = now.minusDays(30).toString()
                            },
                            label = { Text("近30天") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = LocalDate.now()
                                endDate = now.toString()
                                startDate = now.minusDays(90).toString()
                            },
                            label = { Text("近90天") }
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.setFilter(selectedUser, startDate, endDate)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("应用筛选")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                selectedUser = ""
                                startDate = ""
                                endDate = ""
                                viewModel.resetFilter()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重置")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sortedRecords.size < 2) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Text("需要至少两条记录才能显示趋势", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                LineChart(sortedRecords)
            }
        }
    }
    
    MaterialDialog(
        dialogState = startDateDialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }
    ) {
        datepicker(
            initialDate = if (startDate.isNotEmpty()) LocalDate.parse(startDate) else LocalDate.now(),
            title = "选择开始日期"
        ) {
            startDate = it.toString()
        }
    }
    
    MaterialDialog(
        dialogState = endDateDialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }
    ) {
        datepicker(
            initialDate = if (endDate.isNotEmpty()) LocalDate.parse(endDate) else LocalDate.now(),
            title = "选择结束日期"
        ) {
            endDate = it.toString()
        }
    }
}

@Composable
fun LineChart(records: List<Record>) {
    val sysColor = Color(0xFFEF4444)
    val diaColor = Color(0xFF0EA5E9)
    val gridColor = Color.LightGray.copy(alpha = 0.5f)
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(start = 40.dp, bottom = 30.dp, end = 16.dp, top = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val xStep = width / (records.size - 1).coerceAtLeast(1)
                        // Find closest index
                        val index = (offset.x / xStep).let { 
                            if (it.isNaN()) 0 else Math.round(it)
                        }.coerceIn(0, records.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            
            // Calculate Range
            val maxVal = (records.maxOfOrNull { Math.max(it.systolic, it.diastolic) } ?: 200).toFloat() + 10
            val minVal = (records.minOfOrNull { Math.min(it.systolic, it.diastolic) } ?: 50).toFloat() - 10
            val range = maxVal - minVal
            
            val xStep = width / (records.size - 1).coerceAtLeast(1)
            
            // Paint for text
            val textPaint = android.graphics.Paint().apply {
                color = textColor
                textSize = 30f
                textAlign = android.graphics.Paint.Align.RIGHT
                typeface = android.graphics.Typeface.DEFAULT
            }
            
            // Draw Grid and Y-Axis Labels
            val steps = 5
            for (i in 0..steps) {
                val y = height - (i.toFloat() / steps * height)
                val value = minVal + (i.toFloat() / steps * range)
                
                // Grid line
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                
                // Label
                drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    -10f, // Offset to left
                    y + 10f, // Center vertically
                    textPaint
                )
            }
            
            // Draw Paths
            val sysPath = Path()
            val diaPath = Path()
            
            records.forEachIndexed { index, record ->
                val x = index * xStep
                val sysY = height - ((record.systolic - minVal) / range * height)
                val diaY = height - ((record.diastolic - minVal) / range * height)
                
                if (index == 0) {
                    sysPath.moveTo(x, sysY)
                    diaPath.moveTo(x, diaY)
                } else {
                    sysPath.lineTo(x, sysY)
                    diaPath.lineTo(x, diaY)
                }
                
                // Draw points
                drawCircle(color = sysColor, radius = 4.dp.toPx(), center = Offset(x, sysY))
                drawCircle(color = diaColor, radius = 4.dp.toPx(), center = Offset(x, diaY))
            }
            
            drawPath(path = sysPath, color = sysColor, style = Stroke(width = 3.dp.toPx()))
            drawPath(path = diaPath, color = diaColor, style = Stroke(width = 3.dp.toPx()))
            
            // Draw Selection Tooltip
            selectedIndex?.let { index ->
                val record = records[index]
                val x = index * xStep
                
                // Vertical indicator line
                drawLine(
                    color = Color.Gray,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                
                // Tooltip Box
                val tooltipWidth = 320f  // 增加宽度以显示完整信息
                val tooltipHeight = if (record.pulse != null) 180f else 150f  // 根据是否有心率动态调整高度
                val tooltipPadding = 16f
                
                // Determine tooltip position (avoid going off screen)
                var tooltipX = x + 20f
                if (tooltipX + tooltipWidth > width) {
                    tooltipX = x - tooltipWidth - 20f
                }
                val tooltipY = 20f
                
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                )
                
                val tooltipTextPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                
                val dateStr = TimeUtils.formatDate(record.date)
                drawContext.canvas.nativeCanvas.drawText(dateStr, tooltipX + tooltipPadding, tooltipY + 40f, tooltipTextPaint)
                
                tooltipTextPaint.textSize = 28f
                tooltipTextPaint.typeface = android.graphics.Typeface.DEFAULT
                
                var currentY = tooltipY + 75f
                drawContext.canvas.nativeCanvas.drawText("收缩压: ${record.systolic} mmHg", tooltipX + tooltipPadding, currentY, tooltipTextPaint)
                
                currentY += 35f
                drawContext.canvas.nativeCanvas.drawText("舒张压: ${record.diastolic} mmHg", tooltipX + tooltipPadding, currentY, tooltipTextPaint)
                
                // 如果有心率数据，也显示
                if (record.pulse != null) {
                    currentY += 35f
                    drawContext.canvas.nativeCanvas.drawText("心率: ${record.pulse} bpm", tooltipX + tooltipPadding, currentY, tooltipTextPaint)
                }
            }
        }
        
        // Legend
        Row(modifier = Modifier.padding(start = 40.dp, top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(sysColor, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("收缩压", color = sysColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(diaColor, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("舒张压", color = diaColor)
            }
        }
    }
}
