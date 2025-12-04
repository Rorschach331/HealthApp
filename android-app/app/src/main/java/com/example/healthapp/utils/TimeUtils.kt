package com.example.healthapp.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object TimeUtils {
    // 获取客户端时区，如果获取失败则使用东八区
    private val CLIENT_ZONE: ZoneId by lazy {
        try {
            ZoneId.systemDefault()
        } catch (e: Exception) {
            ZoneId.of("Asia/Shanghai")
        }
    }
    
    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 将后端返回的 ISO 时间字符串格式化为客户端时区的时间字符串
     */
    fun formatDate(dateStr: String): String {
        return try {
            // 解析 ISO 8601 格式 (e.g., 2023-11-01T12:00:00Z 或 2023-11-01T12:00:00.000Z)
            val instant = try {
                Instant.parse(dateStr)
            } catch (e: Exception) {
                // Fallback: 尝试解析不带时区的字符串，假设它是 UTC
                val fallbackFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTimePart = dateStr.substringBefore(".")
                LocalDateTime.parse(dateTimePart, fallbackFormatter)
                    .atZone(ZoneId.of("UTC"))
                    .toInstant()
            }
            
            val zonedDateTime = instant.atZone(CLIENT_ZONE)
            zonedDateTime.format(DISPLAY_FORMATTER)
        } catch (e: Exception) {
            dateStr // 解析失败，返回原字符串
        }
    }

    /**
     * 将用户选择的日期和时间（视为客户端时区）转换为 Date 对象（UTC）
     */
    fun toUtcDate(date: LocalDate, time: LocalTime): Date {
        val zonedDateTime = LocalDateTime.of(date, time).atZone(CLIENT_ZONE)
        return Date.from(zonedDateTime.toInstant())
    }

    /**
     * 获取当前客户端时区的 LocalDate
     */
    fun getCurrentLocalDate(): LocalDate {
        return LocalDate.now(CLIENT_ZONE)
    }
    
    /**
     * 获取当前客户端时区的 LocalTime
     */
    fun getCurrentLocalTime(): LocalTime {
        return LocalTime.now(CLIENT_ZONE)
    }

    fun toIsoString(date: Date): String {
        return date.toInstant().toString()
    }
}
