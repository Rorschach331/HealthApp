package com.example.healthapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.model.CreateRecordRequest
import com.example.healthapp.model.Record
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    private val _users = MutableStateFlow<List<String>>(emptyList())
    val users: StateFlow<List<String>> = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        fetchUsers()
        fetchRecords()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _users.value = api.getUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRecords() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Fetch all for now, pagination can be added later
                val response = api.getRecords(pageSize = 100) 
                _records.value = response.data
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun addRecord(systolic: Int, diastolic: Int, pulse: Int?, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.createRecord(CreateRecordRequest(systolic, diastolic, pulse, name))
                fetchRecords()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            try {
                api.deleteRecord(id)
                fetchRecords()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
