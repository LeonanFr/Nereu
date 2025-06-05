package com.example.nereu

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.nereu.ui.components.SearchModalBottomSheetNereu
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

@Composable
fun ShowParameter(
    selectedParameter: String,
    onParameterSelected: (String) -> Unit
) {
    val parameters = listOf("temperature", "pressure", "salinity")

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        parameters.forEach { param ->
            FilterChip(
                selected = selectedParameter == param,
                onClick = { onParameterSelected(param) },
                label = { Text(getParameterName(param)) }
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
    fun EnvironmentalData.getParameterValueLocal(param: String): Float {
        return when (param.lowercase()) {
            "temperature" -> this.temperature ?: 0f
            "pressure" -> this.pressure ?: 0f
            "salinity" -> this.salinity ?: 0f
            else -> 0f
        }
    }

    val validData = environmentalData
        .filter { data ->
            data.timestamp.isNotBlank() && when (selectedParameter) {
                "temperature" -> data.temperature != null
                "pressure" -> data.pressure != null
                "salinity" -> data.salinity != null
                else -> false
            }
        }
        .sortedWith(compareBy { data ->
            var timeInMillis: Long? = null
            val primaryPattern = "EEE MMM dd HH:mm:ss yyyy"
            try {
                val sdf = SimpleDateFormat(primaryPattern, Locale.ENGLISH)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                timeInMillis = sdf.parse(data.timestamp)?.time
            } catch (_: Exception) {
                val fallbackPatterns = listOf(
                    "EEE MMM dd HH:mm:ss zzz yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'"
                )
                for (pattern in fallbackPatterns) {
                    try {
                        val sdfFallback = SimpleDateFormat(pattern, Locale.ENGLISH)
                        if (pattern.endsWith("'Z'")) sdfFallback.timeZone = TimeZone.getTimeZone("UTC")
                        else if (!pattern.contains("zzz") && !pattern.contains("X") && !pattern.contains(" Z")&& !pattern.contains("yyyy-MM-dd HH:mm:ss")) {
                            sdfFallback.timeZone = TimeZone.getTimeZone("UTC")
                        }
                        timeInMillis = sdfFallback.parse(data.timestamp)?.time
                        if (timeInMillis != null) break
                    } catch (_: Exception) {}
                }
            }
            timeInMillis ?: 0L
        })

    var chartData by remember(validData, selectedParameter) {
        val entries = validData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.getParameterValueLocal(selectedParameter))
        }

        val dataSet = LineDataSet(entries, getParameterName(selectedParameter)).apply {
            color = AndroidColor.BLUE
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            valueTextColor = AndroidColor.BLACK
        }
        mutableStateOf(LineData(dataSet))
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = true
                legend.textSize = 12f
                axisRight.isEnabled = false

                axisLeft.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = AndroidColor.DKGRAY
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textSize = 10f
                    textColor = AndroidColor.DKGRAY
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = -65f
                    axisMinimum = 0f
                    axisMaximum = (validData.size - 1).toFloat().coerceAtLeast(0f)


                    valueFormatter = if (validData.isNotEmpty()) {
                        object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val idx = value.toInt().coerceIn(validData.indices)
                                return formatTimestampToHour(validData.getOrNull(idx)?.timestamp ?: "")
                            }
                        }
                    } else {
                        object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return ""
                            }
                        }
                    }
                }
                setExtraOffsets(0f, 0f, 0f, 30f)
            }
        },
        update = { chart ->
            chart.data = chartData
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NereuGraphScreen(viewModel: NereuViewModel, navController: NavController) {
    val data by viewModel.environmentalData.collectAsState()
    val selectedParam by viewModel.selectedParameter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showDatePickerSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Nereu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePickerSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Selecionar Data",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF42A5F5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getParameterName(selectedParam),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Em ${formatDateForTitle(selectedDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ShowParameter(
                selectedParameter = selectedParam,
                onParameterSelected = viewModel::selectParameter
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF0D47A1))
                } else if (data.any { environmentalData ->
                        val value = when (selectedParam.lowercase()) {
                            "temperature" -> environmentalData.temperature
                            "salinity" -> environmentalData.salinity
                            "pressure" -> environmentalData.pressure
                            else -> null
                        }
                        value != null && environmentalData.timestamp.isNotBlank()
                    }) {
                    NereuLineChart(
                        environmentalData = data,
                        selectedParameter = selectedParam,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Nenhum dado encontrado para ${getParameterName(selectedParam)} em ${formatDateForTitle(selectedDate)}.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        viewModel.previousDay()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Text(text = "Dia Anterior", color = Color.White)
                }
                Button(
                    onClick = {
                        viewModel.nextDay()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                ) {
                    Text(text = "Dia Seguinte", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showDatePickerSheet) {
            SearchModalBottomSheetNereu(
                initialIsoDate = selectedDate,
                onDismiss = { showDatePickerSheet = false },
                onIsoDateSelected = { newIsoDate ->
                    viewModel.selectDate(newIsoDate)
                    showDatePickerSheet = false
                }
            )
        }
    }
}