package com.example.menuku.features.inventory

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.testTag
import com.example.menuku.data.models.StockDates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryDateField(day: Long, label: String = "Kedaluwarsa", onDayChange: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().testTag("expiry_date_field")) {
        Text("$label: ${StockDates.format(day)}")
    }
    if (label == "Kedaluwarsa") Text(StockDates.remainingLabel((day - StockDates.todayEpochDay()).toInt()))
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = day * StockDates.MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(enabled = state.selectedDateMillis != null, onClick = {
                    state.selectedDateMillis?.let { onDayChange(it / StockDates.MILLIS_PER_DAY) }
                    open = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Batal") } }
        ) { DatePicker(state = state) }
    }
}
