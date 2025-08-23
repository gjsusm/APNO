package com.example.apno.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val viewModel: ReportsViewModel = viewModel()
    val report by viewModel.salesReport.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Button(onClick = { showStartDatePicker = true }) {
                Text(text = startDate?.let { formatDate(it) } ?: "Select Start Date")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showEndDatePicker = true }) {
                Text(text = endDate?.let { formatDate(it) } ?: "Select End Date")
            }
        }

        Button(
            onClick = {
                if (startDate != null && endDate != null) {
                    viewModel.generateSalesReport(startDate!!, endDate!!)
                }
            },
            enabled = startDate != null && endDate != null
        ) {
            Text("Generate Sales Report")
        }

        report?.let {
            Text("Total Sales: $${it.totalSales}")
            Text("Number of Orders: ${it.numberOfOrders}")
            LazyColumn {
                items(it.orders) { order ->
                    ListItem(
                        headlineContent = { Text("Order on ${formatDate(order.createdAt!!)}") },
                        supportingContent = { Text("Total: $${order.total}") }
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = startDatePickerState.selectedDateMillis?.let { Date(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = endDatePickerState.selectedDateMillis?.let { Date(it) }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

private fun formatDate(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}
