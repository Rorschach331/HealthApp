package com.example.healthapp.model

data class Record(
    val id: Long,
    val date: String,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val name: String?
)

data class RecordResponse(
    val data: List<Record>,
    val meta: Meta
)

data class Meta(
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

data class CreateRecordRequest(
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val name: String,
    val date: String? = null
)
