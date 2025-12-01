package com.example.healthapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var _apiService: HealthApiService? = null
    
    // Default URL, will be overwritten by PreferenceManager
    private var baseUrl: String = "http://10.0.2.2:3000/"

    fun setBaseUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        if (baseUrl != formattedUrl || _apiService == null) {
            baseUrl = formattedUrl
            buildRetrofit()
        }
    }
    
    fun getBaseUrl(): String = baseUrl

    private fun buildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        _apiService = retrofit!!.create(HealthApiService::class.java)
    }

    val apiService: HealthApiService
        get() {
            if (_apiService == null) buildRetrofit()
            return _apiService!!
        }
}
