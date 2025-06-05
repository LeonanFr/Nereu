package com.example.nereu.network

import com.example.nereu.EnvironmentalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NereuService {

    fun getLastEnvironmentalData(
        onSuccess: (EnvironmentalData) -> Unit,
        onFailure: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getLastEnvironmentalData()

                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess(response)
                }

            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure("Erro: ${e.message}")
                }
            }
        }
    }

    fun getEnvironmentalDataByDate(
        date: String,
        onSuccess: (List<EnvironmentalData>) -> Unit,
        onFailure: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getEnvironmentalDataByDate(date)

                val dataList = response.data

                if (dataList.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess(dataList)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onFailure("Nenhum dado disponível para essa data")
                    }
                }

            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure("Erro: ${e.message}")
                }
            }
        }
    }
}
