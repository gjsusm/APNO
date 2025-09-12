package com.example.apno.admin.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apno.R
import com.example.apno.data.model.Category

@Composable
fun CategoryAdminScreen() {
    val viewModel: CategoryAdminViewModel = viewModel()
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedCategory = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(categories) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    supportingContent = { Text("${stringResource(R.string.order)}: ${category.order}") },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                selectedCategory = category
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_category))
                            }
                            IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear))
                            }
                        }
                    }
                )
            }
        }

        if (showDialog) {
            CategoryEditDialog(
                category = selectedCategory,
                onDismiss = { showDialog = false },
                onConfirm = { name, order ->
                    if (selectedCategory == null) {
                        viewModel.addCategory(name, order)
                    } else {
                        viewModel.updateCategory(selectedCategory!!.copy(name = name, order = order))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var order by remember { mutableStateOf(category?.order?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) stringResource(R.string.add_category) else stringResource(R.string.edit_category)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_name)) }
                )
                OutlinedTextField(
                    value = order,
                    onValueChange = { order = it },
                    label = { Text(stringResource(R.string.order)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, order.toIntOrNull() ?: 0) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
