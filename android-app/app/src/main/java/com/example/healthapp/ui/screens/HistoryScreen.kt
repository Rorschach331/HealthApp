package com.example.healthapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.healthapp.model.Record
import com.example.healthapp.utils.TimeUtils
import com.example.healthapp.viewmodel.MainViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    
    val startDateDialogState = rememberMaterialDialogState()
    val endDateDialogState = rememberMaterialDialogState()
    
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState()) // Allow horizontal scrolling
                                ) {
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
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "统计数据",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    "${if (startDate.isNotEmpty()) startDate else "—"} 至 ${if (endDate.isNotEmpty()) endDate else "—"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Use spacedBy instead of SpaceBetween for better look if chips are small
                                ) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("高压均值 ${average.first}") },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = null
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("低压均值 ${average.second}") },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = null
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "第 ${meta.page} / ${meta.totalPages} 页，共 ${meta.total} 条记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (meta.page >= meta.totalPages && records.isNotEmpty()) {
                            Text(
                                "已加载全部",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
fun RecordCard(
    record: Record,
    average: Pair<Int, Int>,
    previousRecord: Record?,
    onDelete: () -> Unit
) {
    val (statusLabel, statusColor) = getStatus(record.systolic, record.diastolic)
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Time, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.medium, // Changed to medium for softer look
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = record.name?.take(1) ?: "?",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Column {
                        Text(record.name ?: "未知", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                TimeUtils.formatDate(record.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                StatusTag(statusLabel, statusColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main Data: BP and Pulse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${record.systolic}",
                        style = MaterialTheme.typography.displaySmall, // Larger font
                        color = statusColor
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${record.diastolic}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = statusColor
                    )
                    Text(
                        text = "mmHg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp).padding(start = 4.dp)
                    )
                }
                
                if (record.pulse != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Text("${record.pulse}", style = MaterialTheme.typography.titleMedium)
                        Text("bpm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Comparisons and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (previousRecord != null) {
                        val sysDiff = record.systolic - previousRecord.systolic
                        val diaDiff = record.diastolic - previousRecord.diastolic
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("较上次", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ComparisonText(sysDiff, diaDiff)
                        }
                    }
                    
                    val sysAvgDiff = record.systolic - average.first
                    val diaAvgDiff = record.diastolic - average.second
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("较均值", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ComparisonText(sysAvgDiff, diaAvgDiff)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun ComparisonText(sysDiff: Int, diaDiff: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "高 ${if (sysDiff > 0) "+" else ""}$sysDiff",
            style = MaterialTheme.typography.bodySmall,
            color = if (sysDiff > 0) MaterialTheme.colorScheme.error else if (sysDiff < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "低 ${if (diaDiff > 0) "+" else ""}$diaDiff",
            style = MaterialTheme.typography.bodySmall,
            color = if (diaDiff > 0) MaterialTheme.colorScheme.error else if (diaDiff < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
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
