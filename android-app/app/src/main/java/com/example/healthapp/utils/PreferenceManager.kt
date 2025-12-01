package com.example.healthapp.utils

import android.content.Context

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("health_app_prefs", Context.MODE_PRIVATE)

    fun getBaseUrl(): String {
        return prefs.getString("base_url", "") ?: ""
    }

    fun saveBaseUrl(url: String) {
        prefs.edit().putString("base_url", url).apply()
    }
    
    fun isFirstRun(): Boolean {
        return getBaseUrl().isEmpty()
    }
    
    fun setConfigured() {
        if (getBaseUrl().isEmpty()) {
            saveBaseUrl("http://10.0.2.2:3000/")
        }
    }
}
