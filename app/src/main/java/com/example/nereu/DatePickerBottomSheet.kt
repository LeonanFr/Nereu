package com.example.nereu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatMillisToDisplayDate(timeInMillis: Long, timeZoneId: String = "UTC"): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
    return sdf.format(Date(timeInMillis))
}

fun formatMillisToIsoDate(timeInMillis: Long, timeZoneId: String = "UTC"): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
    return sdf.format(Date(timeInMillis))
}

fun convertIsoDateToMillis(isoDate: String, timeZoneId: String = "UTC"): Long? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
        sdf.parse(isoDate)?.time
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInputNereu(
    initialSelectedDateMillis: Long?,
    onDateMillisSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    maxDateMillis: Long = System.currentTimeMillis()
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        initialDisplayMode = DisplayMode.Picker,
        yearRange = (2020..currentYear)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null && selectedMillis <= maxDateMillis) {
                    onDateMillisSelected(selectedMillis)
                } else {
                    onDateMillisSelected(null)
                }
                onDismiss()
            }) {
                Text("OK", color = Color(0xFF0D47A1))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDateMillisSelected(null)
                onDismiss()
            }) {
                Text("Cancelar", color = Color(0xFF0D47A1))
            }
        },
        colors = DatePickerDefaults.colors(containerColor = Color.White)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Selecione uma data",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            headline = {
                Text(
                    text = datePickerState.selectedDateMillis?.let { formatMillisToDisplayDate(it) } ?: "Nenhuma data",
                    modifier = Modifier.padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(0xFF0D47A1),
                todayDateBorderColor = Color(0xFF42A5F5),
                todayContentColor = Color(0xFF0D47A1)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchModalBottomSheetNereu(
    initialIsoDate: String,
    onDismiss: () -> Unit,
    onIsoDateSelected: (String) -> Unit
) {
    var selectedMillisFromPicker by remember {
        mutableStateOf(convertIsoDateToMillis(initialIsoDate))
    }

    val displayDateText = remember(selectedMillisFromPicker) {
        selectedMillisFromPicker?.let { formatMillisToDisplayDate(it) } ?: ""
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecionar Data Específica",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = Color.LightGray)

            OutlinedTextField(
                value = displayDateText,
                onValueChange = {},
                label = { Text("Data Selecionada", color = Color.DarkGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showDatePicker = true },
                enabled = false,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledContainerColor = Color(0xFFF0F0F0),
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.DarkGray,
                ),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Abrir Calendário",
                        tint = Color(0xFF0D47A1)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedMillisFromPicker?.let { millis ->
                        onIsoDateSelected(formatMillisToIsoDate(millis))
                    }
                    onDismiss()
                },
                enabled = selectedMillisFromPicker != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Confirmar Data",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showDatePicker) {
                DatePickerModalInputNereu(
                    initialSelectedDateMillis = selectedMillisFromPicker ?: convertIsoDateToMillis(initialIsoDate) ?: System.currentTimeMillis(),
                    onDateMillisSelected = { newMillis ->
                        selectedMillisFromPicker = newMillis
                        showDatePicker = false
                    },
                    onDismiss = {
                        showDatePicker = false
                    }
                )
            }
        }
    }
}