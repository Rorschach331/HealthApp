package com.example.healthapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.model.CreateRecordRequest
import com.example.healthapp.model.Meta
import com.example.healthapp.model.Record
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {
    private val api get() = RetrofitClient.apiService

    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    private val _users = MutableStateFlow<List<String>>(emptyList())
    val users: StateFlow<List<String>> = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    
    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore
    
    private val _meta = MutableStateFlow(Meta(0, 1, 20, 0))
    val meta: StateFlow<Meta> = _meta
    
    // Filter state
    private val _filterName = MutableStateFlow("")
    val filterName: StateFlow<String> = _filterName
    
    private val _filterStart = MutableStateFlow(java.time.LocalDate.now().minusDays(30).toString())
    val filterStart: StateFlow<String> = _filterStart
    
    private val _filterEnd = MutableStateFlow(java.time.LocalDate.now().toString())
    val filterEnd: StateFlow<String> = _filterEnd
    
    private var currentPage = 1
    private val pageSize = 20

    init {
        fetchUsers()
        // fetchRecords will be called inside fetchUsers if a default user is selected, 
        // otherwise we call it here. But since fetchUsers is async, we might want to call fetchRecords anyway 
        // to show some data (even if empty user filter initially).
        // However, if we want to enforce "default user", we should wait for users.
        // Let's call fetchRecords here with the default date filter.
        fetchRecords(reset = true)
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val userList = api.getUsers()
                _users.value = userList
                
                // Set default user if not set and users exist
                if (_filterName.value.isEmpty() && userList.isNotEmpty()) {
                    _filterName.value = userList[0]
                    // Re-fetch records with the new user filter
                    fetchRecords(reset = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun setFilter(name: String = _filterName.value, start: String = _filterStart.value, end: String = _filterEnd.value) {
        _filterName.value = name
        _filterStart.value = start
        _filterEnd.value = end
        fetchRecords(reset = true)
    }
    
    fun resetFilter() {
        // Reset to default: first user (if exists) and last 30 days
        if (_users.value.isNotEmpty()) {
            _filterName.value = _users.value[0]
        } else {
            _filterName.value = ""
        }
        _filterStart.value = java.time.LocalDate.now().minusDays(30).toString()
        _filterEnd.value = java.time.LocalDate.now().toString()
        fetchRecords(reset = true)
    }

    fun fetchRecords(reset: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _loading.value = true
                currentPage = 1
            } else {
                _loadingMore.value = true
            }
            
            try {
                val startParam = if (_filterStart.value.isNotEmpty()) {
                    "${_filterStart.value}T00:00:00.000Z"
                } else null
                
                val endParam = if (_filterEnd.value.isNotEmpty()) {
                    "${_filterEnd.value}T23:59:59.999Z"
                } else null
                
                val nameParam = if (_filterName.value.isNotEmpty()) {
                    _filterName.value
                } else null
                
                val response = api.getRecords(
                    start = startParam,
                    end = endParam,
                    name = nameParam,
                    page = currentPage,
                    pageSize = pageSize
                )
                
                if (reset) {
                    _records.value = response.data
                } else {
                    _records.value = _records.value + response.data
                }
                
                _meta.value = response.meta
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
                _loadingMore.value = false
            }
        }
    }
    
    fun loadMore() {
        if (_loadingMore.value || _loading.value) return
        if (currentPage >= _meta.value.totalPages) return
        
        currentPage++
        fetchRecords(reset = false)
    }

    fun addRecord(systolic: Int, diastolic: Int, pulse: Int?, name: String, date: Date = Date(), onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.createRecord(CreateRecordRequest(systolic, diastolic, pulse, name))
                fetchRecords(reset = true)
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
                fetchRecords(reset = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun refreshData() {
        fetchUsers()
        fetchRecords(reset = true)
    }
}
