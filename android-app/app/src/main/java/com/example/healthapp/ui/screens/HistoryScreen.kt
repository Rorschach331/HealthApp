package com.example.healthapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.model.Record
import com.example.healthapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: MainViewModel = viewModel()) {
    val records by viewModel.records.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("历史记录", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(records) { record ->
                RecordCard(record, onDelete = { showDeleteDialog = record.id })
            }
        }
    }
}

@Composable
fun RecordCard(record: Record, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = record.name?.take(1) ?: "?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(record.name ?: "未知", style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatDate(record.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusTag(record.systolic, record.diastolic)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${record.systolic}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = getStatusColor(record.systolic, record.diastolic)
                )
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${record.diastolic}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = getStatusColor(record.systolic, record.diastolic)
                )
                Text(
                    text = " mmHg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            if (record.pulse != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("心率: ${record.pulse}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StatusTag(sys: Int, dia: Int) {
    val (label, color) = when {
        sys >= 140 || dia >= 90 -> "高血压" to MaterialTheme.colorScheme.error
        sys >= 130 || dia >= 90 -> "偏高" to Color(0xFFF59E0B) // Warning
        else -> "正常" to MaterialTheme.colorScheme.primary // Success
    }
    
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

@Composable
fun getStatusColor(sys: Int, dia: Int): Color {
    return when {
        sys >= 140 || dia >= 90 -> MaterialTheme.colorScheme.error
        sys >= 130 || dia >= 90 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.onSurface
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
