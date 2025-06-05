package com.example.nereu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nereu.network.NereuService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NereuViewModel : ViewModel() {

    private val _environmentalData = MutableStateFlow<List<EnvironmentalData>>(emptyList())
    val environmentalData: StateFlow<List<EnvironmentalData>> = _environmentalData

    private val _lastData = MutableStateFlow<EnvironmentalData?>(null)
    val lastData : StateFlow<EnvironmentalData?> = _lastData

    private var _previousData: EnvironmentalData? = null

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedDate = MutableStateFlow<String>(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _selectedParameter = MutableStateFlow<String>("temperature")
    val selectedParameter: StateFlow<String> = _selectedParameter

    init {
        val todayMillis = System.currentTimeMillis()
        fetchDataForDay(todayMillis)
        loadLatestData()
    }

    fun selectParameter(param: String) {
        _selectedParameter.value = param
    }

    fun selectDate(date: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        try {
            val newDate = sdf.parse(date)
            val today = sdf.parse(getTodayDateString())
            if (newDate != null && today != null) {
                if (!newDate.after(today)) {
                    _selectedDate.value = date
                    fetchDataForDateString(date)
                }
            }
        } catch (_: Exception) {
            println("Invalid date format")
        }
    }
    fun previousDay() {
        selectedDate.value.let { currentDateStr ->
            val currentDateMillis = dateStringToMillis(currentDateStr)
            val previousDateMillis = currentDateMillis - 24 * 60 * 60 * 1000
            val newDateStr = millisToDateString(previousDateMillis)
            selectDate(newDateStr)
        }
    }

    fun nextDay() {
        selectedDate.value.let { currentDateStr ->
            val currentDateMillis = dateStringToMillis(currentDateStr)
            val nextDateMillis = currentDateMillis + 24 * 60 * 60 * 1000
            val nowMillis = System.currentTimeMillis()
            val todayMillis = millisToDateString(nowMillis).let(::dateStringToMillis)

            if (nextDateMillis <= todayMillis) {
                val newDateStr = millisToDateString(nextDateMillis)
                selectDate(newDateStr)
            }
        }
    }


    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadLatestData() {
        viewModelScope.launch {
            NereuService.getLastEnvironmentalData(
                onSuccess = { data ->

                    if (_lastData.value == null || data.timestamp > _lastData.value!!.timestamp) {
                        _previousData = _lastData.value
                        _lastData.value = data
                    }
                    _errorMessage.value = null
                },
                onFailure = { error ->
                    _errorMessage.value = error
                }
            )
        }
    }

    fun fetchDataForDay(dateMillis: Long) {
        val dateStr = formatDateToUtcString(dateMillis)
        viewModelScope.launch {
            NereuService.getEnvironmentalDataByDate(dateStr,
                onSuccess = { dataList ->
                    _environmentalData.value = dataList
                    _errorMessage.value = null
                },
                onFailure = { error ->
                    _environmentalData.value = emptyList()
                    _errorMessage.value = error
                }
            )
        }
    }

    private fun fetchDataForDateString(dateStr: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _environmentalData.value = emptyList()
            NereuService.getEnvironmentalDataByDate(dateStr,
                onSuccess = { dataList ->
                    _environmentalData.value = dataList
                    _errorMessage.value = null
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _environmentalData.value = emptyList()
                    _errorMessage.value = error
                    _isLoading.value = false
                }
            )
        }
    }

    private fun formatDateToUtcString(dateMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(dateMillis))
    }
}
