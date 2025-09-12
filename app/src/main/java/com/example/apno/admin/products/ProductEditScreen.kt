package com.example.apno.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.apno.R
import com.example.apno.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: String?,
    navController: NavController
) {
    val viewModel: ProductAdminViewModel = viewModel()

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        } else {
            viewModel.resetSelectedProduct()
        }
    }

    val product by viewModel.selectedProduct.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            description = it.description
            price = it.price.toString()
            selectedCategoryId = it.categoryId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(if (productId == null) stringResource(R.string.add_product) else stringResource(R.string.edit_product), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.product_name)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text(stringResource(R.string.price)) }, modifier = Modifier.fillMaxWidth())

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategoryId = category.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val productToSave = (product ?: Product()).copy(
                    name = name,
                    description = description,
                    price = price.toDoubleOrNull() ?: 0.0,
                    categoryId = selectedCategoryId
                )
                viewModel.saveProduct(productToSave)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_product))
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.onSaveComplete()
            navController.popBackStack()
        }
    }
}
