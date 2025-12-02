package com.example.healthapp

import android.app.Application

class HealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.healthapp.api.RetrofitClient.init(this)
    }
}
