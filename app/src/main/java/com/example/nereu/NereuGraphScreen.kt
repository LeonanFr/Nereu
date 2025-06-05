package com.example.nereu

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShowParameter(
    selectedParameter: String,
    onParameterSelected: (String) -> Unit
) {
    val parameters = listOf("temperature", "pressure", "salinity")

    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        parameters.forEach { param ->
            FilterChip(
                selected = selectedParameter == param,
                onClick = { onParameterSelected(param) },
                label = { Text(param.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
fun NereuLineChart(
    environmentalData: List<EnvironmentalData>,
    selectedParameter: String,
    modifier: Modifier = Modifier
) {

    fun EnvironmentalData.getParameterValue(param: String): Float {
        return when (param) {
            "temperature" -> this.temperature ?: 0f
            "pressure" -> this.pressure ?: 0f
            "salinity" -> this.salinity ?: 0f
            else -> 0f
        }
    }

    val sortedData = environmentalData.sortedBy { it.timestamp }

    var chartData by remember { mutableStateOf(LineData()) }

    LaunchedEffect(sortedData, selectedParameter) {
        val entries = sortedData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.getParameterValue(selectedParameter))
        }

        val dataSet = LineDataSet(entries, "Parâmetro: ${selectedParameter.replaceFirstChar { it.uppercase() }}").apply {
            color = Color.BLUE
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            valueTextColor = Color.BLACK
        }
        chartData = LineData(dataSet)
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = false
                axisRight.isEnabled = false

                axisLeft.apply {
                    isEnabled = true
                    textSize = 14f
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textSize = 14f
                    setDrawGridLines(false)
                    granularity = 1f
                    axisMinimum = 0f
                    axisMaximum = (sortedData.size - 1).toFloat().coerceAtLeast(0f)
                    valueFormatter = object : ValueFormatter() {
                        private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt().coerceIn(sortedData.indices)
                            val date = Date(sortedData[idx].timestamp)
                            return sdf.format(date)
                        }
                    }
                }
            }
        },
        update = { chart ->
            chart.data = chartData
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun NereuGraphScreen(viewModel: NereuViewModel) {
    val data by viewModel.environmentalData.collectAsState()
    val selectedParam by viewModel.selectedParameter.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ShowParameter(selectedParameter = selectedParam, onParameterSelected = viewModel::selectParameter)
        Spacer(modifier = Modifier.height(12.dp))
        NereuLineChart(
            environmentalData = data,
            selectedParameter = selectedParam,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
