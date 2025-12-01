package com.example.healthapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.model.Record
import com.example.healthapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChartScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    
    // Sort by date ascending for chart
    val sortedRecords = records.sortedBy { it.date }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("趋势分析", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        
        if (sortedRecords.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("需要至少两条记录才能显示趋势", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                LineChart(sortedRecords)
            }
        }
    }
}

@Composable
fun LineChart(records: List<Record>) {
    val sysColor = Color(0xFFEF4444)
    val diaColor = Color(0xFF0EA5E9)
    
    Canvas(modifier = Modifier.fillMaxSize().padding(start = 30.dp, bottom = 30.dp)) {
        val width = size.width
        val height = size.height
        
        val maxVal = (records.maxOfOrNull { Math.max(it.systolic, it.diastolic) } ?: 200).toFloat() + 20
        val minVal = (records.minOfOrNull { Math.min(it.systolic, it.diastolic) } ?: 50).toFloat() - 20
        val range = maxVal - minVal
        
        val xStep = width / (records.size - 1)
        
        // Draw Axes
        drawLine(
            color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f
        )
        
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
    }
    
    // Legend
    Row(modifier = Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(sysColor, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("收缩压", color = sysColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
