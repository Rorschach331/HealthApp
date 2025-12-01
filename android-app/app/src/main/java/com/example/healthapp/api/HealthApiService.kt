package com.example.healthapp.api

import com.example.healthapp.model.CreateRecordRequest
import com.example.healthapp.model.Record
import com.example.healthapp.model.RecordResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HealthApiService {
    @GET("api/records")
    suspend fun getRecords(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("name") name: String? = null,
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 20
    ): RecordResponse

    @POST("api/records")
    suspend fun createRecord(@Body request: CreateRecordRequest): Record

    @DELETE("api/records/{id}")
    suspend fun deleteRecord(@Path("id") id: Long)

    @GET("api/users")
    suspend fun getUsers(): List<String>
}
