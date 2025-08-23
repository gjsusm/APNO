package com.example.apno.admin.toppings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apno.data.model.Topping

@Composable
fun ToppingAdminScreen() {
    val viewModel: ToppingAdminViewModel = viewModel()
    val toppings by viewModel.toppings.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTopping by remember { mutableStateOf<Topping?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedTopping = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Topping")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(toppings) { topping ->
                ListItem(
                    headlineContent = { Text(topping.name) },
                    supportingContent = { Text(String.format("$%.2f", topping.price)) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                selectedTopping = topping
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteTopping(topping.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
            }
        }

        if (showDialog) {
            ToppingEditDialog(
                topping = selectedTopping,
                onDismiss = { showDialog = false },
                onConfirm = { name, price ->
                    if (selectedTopping == null) {
                        viewModel.addTopping(name, price)
                    } else {
                        viewModel.updateTopping(selectedTopping!!.copy(name = name, price = price))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ToppingEditDialog(
    topping: Topping?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(topping?.name ?: "") }
    var price by remember { mutableStateOf(topping?.price?.toString() ?: "0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (topping == null) "Add Topping" else "Edit Topping") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Topping Name") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
