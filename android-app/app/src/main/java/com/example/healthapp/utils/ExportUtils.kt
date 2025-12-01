package com.example.healthapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.healthapp.model.Record
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExportUtils {
    
    /**
     * 导出为 Excel 文件
     */
    fun exportToExcel(context: Context, records: List<Record>, fileName: String = "health_data"): File? {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("血压记录")
            
            // 创建标题行样式
            val headerCellStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont().apply {
                    bold = true
                    fontHeightInPoints = 12
                }
                setFont(font)
                alignment = HorizontalAlignment.CENTER
            }
            
            // 创建标题行
            val headerRow = sheet.createRow(0)
            val headers = listOf("日期时间", "姓名", "收缩压(mmHg)", "舒张压(mmHg)", "心率(bpm)", "状态")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    cellStyle = headerCellStyle
                }
            }
            
            // 填充数据
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(TimeUtils.formatDate(record.date))
                row.createCell(1).setCellValue(record.name ?: "")
                row.createCell(2).setCellValue(record.systolic.toDouble())
                row.createCell(3).setCellValue(record.diastolic.toDouble())
                row.createCell(4).setCellValue(record.pulse?.toDouble() ?: 0.0)
                row.createCell(5).setCellValue(getStatusText(record.systolic, record.diastolic))
            }
            
            // 自动调整列宽
            for (i in 0 until headers.size) {
                sheet.autoSizeColumn(i)
                // 增加一点额外宽度
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512)
            }
            
            // 保存文件
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, "$fileName.xlsx")
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 导出为 PDF 文件
     */
    fun exportToPdf(context: Context, records: List<Record>, fileName: String = "health_report"): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }
            
            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            
            var yPos = 50f
            
            // 标题
            canvas.drawText("血压健康报告", 50f, yPos, titlePaint)
            yPos += 40f
            
            // 统计信息
            if (records.isNotEmpty()) {
                val avgSys = records.map { it.systolic }.average().toInt()
                val avgDia = records.map { it.diastolic }.average().toInt()
                val avgPulse = records.mapNotNull { it.pulse }.takeIf { it.isNotEmpty() }?.average()?.toInt()
                
                canvas.drawText("记录总数: ${records.size}", 50f, yPos, paint)
                yPos += 20f
                canvas.drawText("平均收缩压: $avgSys mmHg", 50f, yPos, paint)
                yPos += 20f
                canvas.drawText("平均舒张压: $avgDia mmHg", 50f, yPos, paint)
                yPos += 20f
                if (avgPulse != null) {
                    canvas.drawText("平均心率: $avgPulse bpm", 50f, yPos, paint)
                    yPos += 20f
                }
                yPos += 10f
            }
            
            // 表头
            canvas.drawText("日期时间", 50f, yPos, paint)
            canvas.drawText("姓名", 200f, yPos, paint)
            canvas.drawText("收缩压", 280f, yPos, paint)
            canvas.drawText("舒张压", 360f, yPos, paint)
            canvas.drawText("心率", 440f, yPos, paint)
            yPos += 20f
            
            // 数据行
            records.take(30).forEach { record -> // 限制30条以免超出页面
                canvas.drawText(TimeUtils.formatDate(record.date), 50f, yPos, paint)
                canvas.drawText(record.name ?: "", 200f, yPos, paint)
                canvas.drawText("${record.systolic}", 280f, yPos, paint)
                canvas.drawText("${record.diastolic}", 360f, yPos, paint)
                canvas.drawText("${record.pulse ?: "-"}", 440f, yPos, paint)
                yPos += 18f
                
                if (yPos > 800f) return@forEach // 防止超出页面
            }
            
            pdfDocument.finishPage(page)
            
            // 保存文件
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, "$fileName.pdf")
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 分享文件
     */
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                file.name.endsWith(".pdf") -> "application/pdf"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "分享文件"))
    }
    
    private fun getStatusText(sys: Int, dia: Int): String {
        return when {
            sys >= 140 || dia >= 90 -> "高血压"
            sys >= 130 || dia >= 85 -> "偏高"
            else -> "正常"
        }
    }
}
