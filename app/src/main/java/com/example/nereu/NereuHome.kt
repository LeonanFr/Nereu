package com.example.nereu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nereu.ui.components.SearchModalBottomSheetNereu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NereuApp(viewModel: NereuViewModel, navController: NavController) {
    val environmentalData by viewModel.environmentalData.collectAsState()
    val lastData by viewModel.lastData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedParameter by viewModel.selectedParameter.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDatePickerSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nereu",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showDatePickerSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Selecionar Data por Calendário"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = "Erro: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            val cardsData = listOf(
                ParameterData(
                    "temperature",
                    lastData?.temperature,
                    lastData?.timestamp ?: ""
                ),
                ParameterData(
                    "salinity",
                    lastData?.salinity,
                    lastData?.timestamp ?: ""
                ),
                ParameterData(
                    "pressure",
                    lastData?.pressure,
                    lastData?.timestamp ?: ""
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cardsData) { parameterData ->
                    ParameterCard(parameterData,
                        onCardClick = { selectedParamName ->
                            viewModel.selectParameter(selectedParamName)
                            navController.navigate(Destinations.GRAPH_SCREEN)
                        })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ParameterSelector(
                parameters = listOf("temperature", "salinity", "pressure"),
                selectedParameter = selectedParameter,
                onParameterSelected = { viewModel.selectParameter(it) }
            )

            Box(modifier = Modifier.weight(1f)) {
                ParameterSimpleList(
                    environmentalData = environmentalData,
                    selectedParameter = selectedParameter,
                    isLoading = isLoading,
                    date = selectedDate
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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

data class ParameterData(
    val name: String,
    val current: Float?,
    val timestamp: String
)

@Composable
fun ParameterCard(parameterData: ParameterData,
                  onCardClick: (String) -> Unit) {
    val time = formatTimestampToHour(parameterData.timestamp)
    val valueText = parameterData.current?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "-"

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(320.dp)
            .clickable { onCardClick(parameterData.name) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = getImageForParameter(parameterData.name)),
                    contentDescription = parameterData.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                ) {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = getParameterLabel(parameterData.name),
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "${getParameterName(parameterData.name)} às $time",
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Text(
                    text = valueText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ParameterSelector(
    parameters: List<String>,
    selectedParameter: String,
    onParameterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(parameters) { parameter ->
            val isSelected = parameter == selectedParameter
            Button(
                onClick = { onParameterSelected(parameter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF1565C0) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(24.dp),
                border = if (!isSelected) BorderStroke(1.dp, Color(0xFF1565C0)) else null,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 8.dp,
                    focusedElevation = 4.dp,
                    hoveredElevation = 4.dp
                ),
                modifier = Modifier
                    .height(40.dp)
                    .defaultMinSize(minWidth = 80.dp)
            ) {
                Text(
                    text = getParameterName(parameter),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ParameterSimpleList(
    environmentalData: List<EnvironmentalData>,
    selectedParameter: String,
    isLoading: Boolean = false,
    date: String
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Lista de ${getParameterName(selectedParameter)} em ${formatDateForTitle(date)}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalDivider()

        if(isLoading){
            Column (modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = Color(0xFF0D47A1))
            }
        } else if(environmentalData.isEmpty()){
            Column (modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("Nenhum dado disponível para ${getParameterName(selectedParameter)} em ${formatDateForTitle(date)}.")
            }
        } else{
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(environmentalData) { data ->
                    ParameterSimpleRow(data, selectedParameter)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ParameterSimpleRow(data: EnvironmentalData, parameter: String) {
    val value = when (parameter.lowercase()) {
        "temperature" -> data.temperature
        "salinity" -> data.salinity
        "pressure" -> data.pressure
        else -> null
    }

    val valueText = value?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "-"
    val timeText = formatTimestampToHour(data.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = getImageForParameter(parameter)),
            contentDescription = parameter,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = valueText,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = timeText,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

fun formatDateForTitle(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        if (parsedDate != null) outputFormat.format(parsedDate) else date
    } catch (_: Exception) {
        date
    }
}

@Composable
fun getImageForParameter(name: String): Int {
    return when (name.lowercase()) {
        "temperature" -> R.drawable.temperature
        "salinity" -> R.drawable.salinity
        "pressure" -> R.drawable.pressure
        else -> R.drawable.ic_launcher_foreground
    }
}

fun getParameterLabel(name: String): String {
    return when (name.lowercase()) {
        "temperature" -> "Temperatura"
        "salinity" -> "Salinidade"
        "pressure" -> "Pressão"
        else -> name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun getParameterName(name: String): String {
    return when (name.lowercase()) {
        "temperature" -> "Temperatura"
        "salinity" -> "Salinidade"
        "pressure" -> "Pressão"
        else -> name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun formatTimestampToHour(timestamp: String): String {
    if (timestamp.isBlank()) return "--:--"

    val cleanedTimestamp = timestamp.trim()
    var date: Date? = null

    val primaryPattern = "EEE MMM dd HH:mm:ss yyyy"
    try {
        val sdf = SimpleDateFormat(primaryPattern, Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        date = sdf.parse(cleanedTimestamp)
    } catch (_: Exception) {
        val fallbackPatterns = listOf(
            "EEE MMM dd HH:mm:ss zzz yyyy",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (pattern in fallbackPatterns) {
            try {
                val sdfFallback = SimpleDateFormat(pattern, Locale.ENGLISH)
                if (pattern.endsWith("'Z'")) {
                    sdfFallback.timeZone = TimeZone.getTimeZone("UTC")
                } else if (!pattern.contains("zzz") && !pattern.contains("X") && !pattern.contains(" Z")) {
                    sdfFallback.timeZone = TimeZone.getTimeZone("UTC")
                }
                date = sdfFallback.parse(cleanedTimestamp)
                if (date != null) break
            } catch (_: Exception) {
            }
        }
    }

    return if (date != null) {
        try {
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
            outputFormat.format(date)
        } catch (e: Exception) {
            System.err.println("NEREU_APP_HOME: Erro ao formatar data final '$date' para HH:mm : ${e.message}")
            "--:--"
        }
    } else {
        System.err.println("NEREU_APP_HOME: FALHA TOTAL AO PARSEAR TIMESTAMP: '$cleanedTimestamp'.")
        "--:--"
    }
}

fun dateStringToMillis(dateString: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

fun millisToDateString(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}