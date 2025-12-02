package com.example.healthapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var _apiService: HealthApiService? = null
    private var context: android.content.Context? = null
    
    // Default URL, will be overwritten by PreferenceManager
    private var baseUrl: String = "http://10.0.2.2:3000/"

    fun init(context: android.content.Context) {
        this.context = context.applicationContext
    }

    fun setBaseUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        if (baseUrl != formattedUrl || _apiService == null) {
            baseUrl = formattedUrl
            buildRetrofit()
        }
    }
    
    fun getBaseUrl(): String = baseUrl

    private fun buildRetrofit() {
        val clientBuilder = okhttp3.OkHttpClient.Builder()
        
        context?.let { ctx ->
            // 1. 请求拦截器：添加 Token
            clientBuilder.addInterceptor { chain ->
                val prefs = com.example.healthapp.utils.PreferenceManager(ctx)
                val token = prefs.getAuthToken()
                val requestBuilder = chain.request().newBuilder()
                if (token.isNotEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }

            // 2. Authenticator：处理 401 自动刷新
            clientBuilder.authenticator { _, response ->
                // 避免死循环：如果重试次数超过限制，放弃
                if (responseCount(response) >= 3) {
                    return@authenticator null
                }

                val prefs = com.example.healthapp.utils.PreferenceManager(ctx)
                val code = prefs.getAuthCode()
                
                // 如果没有授权码，无法刷新，直接返回 null
                if (code.isEmpty()) {
                    return@authenticator null
                }

                try {
                    // 同步调用登录接口获取新 Token
                    // 注意：这里使用一个新的 OkHttpClient 实例或者纯粹的 Request，避免使用拦截器链
                    val loginUrl = "${baseUrl}api/auth/login"
                    val jsonBody = "{\"code\": \"$code\"}"
                    val requestBody = okhttp3.RequestBody.create(
                        okhttp3.MediaType.parse("application/json; charset=utf-8"), 
                        jsonBody
                    )
                    
                    val loginRequest = okhttp3.Request.Builder()
                        .url(loginUrl)
                        .post(requestBody)
                        .build()

                    val loginClient = okhttp3.OkHttpClient() // 独立的 Client
                    val loginResponse = loginClient.newCall(loginRequest).execute()

                    if (loginResponse.isSuccessful) {
                        val responseBody = loginResponse.body()?.string()
                        if (responseBody != null) {
                            val json = org.json.JSONObject(responseBody)
                            val newToken = json.optString("token")
                            
                            if (newToken.isNotEmpty()) {
                                // 保存新 Token
                                prefs.saveAuthToken(newToken)
                                
                                // 使用新 Token 重试原请求
                                return@authenticator response.request().newBuilder()
                                    .header("Authorization", "Bearer $newToken")
                                    .build()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                null // 刷新失败
            }
        }

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        _apiService = retrofit!!.create(HealthApiService::class.java)
    }

    val apiService: HealthApiService
        get() {
            if (_apiService == null) buildRetrofit()
            return _apiService!!
        }

    private fun responseCount(response: okhttp3.Response): Int {
        var result = 1
        var prior = response.priorResponse()
        while (prior != null) {
            result++
            prior = prior.priorResponse()
        }
        return result
    }
}
