package com.example.healthapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.model.Record
import com.example.healthapp.viewmodel.MainViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val meta by viewModel.meta.collectAsState()
    
    val filterName by viewModel.filterName.collectAsState()
    val filterStart by viewModel.filterStart.collectAsState()
    val filterEnd by viewModel.filterEnd.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = loading)
    val listState = rememberLazyListState()
    
    // Sync local filter state with ViewModel
    LaunchedEffect(filterName, filterStart, filterEnd) {
        selectedUser = filterName
        startDate = filterStart
        endDate = filterEnd
    }
    
    // Calculate average
    val average = remember(records) {
        if (records.isEmpty()) {
            Pair(0, 0)
        } else {
            val avgSys = records.map { it.systolic }.average().toInt()
            val avgDia = records.map { it.diastolic }.average().toInt()
            Pair(avgSys, avgDia)
        }
    }
    
    // Detect scroll to bottom for pagination
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= records.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(showDeleteDialog!!)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.fetchRecords(reset = true) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("历史记录", style = MaterialTheme.typography.headlineMedium)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                                    OutlinedTextField(
                                        value = startDate.ifEmpty { "开始日期" },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("开始") },
                                        modifier = Modifier.weight(1f).clickable {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    )
                                    
                                    OutlinedTextField(
                                        value = endDate.ifEmpty { "结束日期" },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("结束") },
                                        modifier = Modifier.weight(1f).clickable {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    endDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    )
                                }
                                
                                // Quick Filters
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                            cal.add(Calendar.DAY_OF_YEAR, -7)
                                            startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                        },
                                        label = { Text("近7天") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                            cal.add(Calendar.DAY_OF_YEAR, -30)
                                            startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                        },
                                        label = { Text("近30天") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                            cal.add(Calendar.DAY_OF_YEAR, -90)
                                            startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
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
                }
            }
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Card
                if (records.isNotEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "统计数据",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "${if (startDate.isNotEmpty()) startDate else "—"} 至 ${if (endDate.isNotEmpty()) endDate else "—"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("高压均值 ${average.first}") }
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("低压均值 ${average.second}") }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Records
                items(records) { record ->
                    val index = records.indexOf(record)
                    val previousRecord = records.getOrNull(index + 1)
                    
                    RecordCard(
                        record = record,
                        average = average,
                        previousRecord = previousRecord,
                        onDelete = { showDeleteDialog = record.id }
                    )
                }
                
                // Loading More Indicator
                if (loadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                // Footer
                item {
                    Text(
                        "第 ${meta.page} / ${meta.totalPages} 页，共 ${meta.total} 条记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (meta.page >= meta.totalPages && records.isNotEmpty()) {
                        Text(
                            "已加载全部",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    record: Record,
    average: Pair<Int, Int>,
    previousRecord: Record?,
    onDelete: () -> Unit
) {
    val (statusLabel, statusColor) = getStatus(record.systolic, record.diastolic)
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = record.name?.take(1) ?: "?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Column {
                        Text(record.name ?: "未知", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatDate(record.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                StatusTag(statusLabel, statusColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Blood Pressure
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${record.systolic}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = statusColor
                )
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${record.diastolic}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = statusColor
                )
                Text(
                    text = " mmHg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            // Pulse
            if (record.pulse != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Text("心率: ${record.pulse}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            // Comparison with previous
            if (previousRecord != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val sysDiff = record.systolic - previousRecord.systolic
                val diaDiff = record.diastolic - previousRecord.diastolic
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("较上次:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ComparisonChip("高压", sysDiff)
                    ComparisonChip("低压", diaDiff)
                }
            }
            
            // Comparison with average
            Spacer(modifier = Modifier.height(4.dp))
            val sysAvgDiff = record.systolic - average.first
            val diaAvgDiff = record.diastolic - average.second
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("较均值:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ComparisonChip("高压", sysAvgDiff)
                ComparisonChip("低压", diaAvgDiff)
            }

            // Delete Button
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ComparisonChip(label: String, diff: Int) {
    val (color, icon) = when {
        diff > 0 -> MaterialTheme.colorScheme.error to Icons.Default.ArrowUpward
        diff < 0 -> MaterialTheme.colorScheme.primary to Icons.Default.ArrowDownward
        else -> MaterialTheme.colorScheme.outline to null
    }
    
    AssistChip(
        onClick = {},
        label = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                }
                Text("$label ${if (diff == 0) "持平" else "${if (diff > 0) "+" else ""}$diff"}")
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

@Composable
fun StatusTag(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun getStatus(sys: Int, dia: Int): Pair<String, Color> {
    return when {
        sys >= 140 || dia >= 90 -> "高血压" to Color(0xFFEF4444)
        sys >= 130 || dia >= 85 -> "偏高" to Color(0xFFF59E0B)
        else -> "正常" to Color(0xFF22C55E)
    }
}

fun formatDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = input.parse(dateStr)
        val output = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        output.format(date!!)
    } catch (e: Exception) {
        dateStr
    }
}
