package com.example.apno.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apno.data.model.OrderItem
import com.example.apno.data.model.Product
import com.example.apno.data.model.Topping
import com.example.apno.user.UserManager

@Composable
fun PosScreen(onNavigateToAdmin: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val toppings by viewModel.toppings.collectAsState()
    val selectedProduct by viewModel.showAddToCartDialog.collectAsState()
    val orderState by viewModel.orderState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .weight(2f)
                .padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategorySelector(categories = categories, onCategorySelected = { category ->
                        viewModel.fetchProductsForCategory(category.id)
                    })
                    if (UserManager.currentUser?.role == "admin") {
                        Button(onClick = onNavigateToAdmin) {
                            Text("Admin")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProductGrid(products = products, onProductSelected = { product ->
                    viewModel.onProductSelected(product)
                })
            }
            SideCart(
                modifier = Modifier.weight(1f),
                items = cartItems,
                subtotal = viewModel.cartSubtotal,
                tax = viewModel.tax,
                total = viewModel.cartTotal,
                orderState = orderState,
                onClear = { viewModel.clearCart() },
                onSave = { /* TODO */ },
                onPay = { viewModel.showPaymentDialog() }
            )
        }

        if (viewModel.showPaymentDialog.collectAsState().value) {
            PaymentDialog(
                total = viewModel.cartTotal,
                onDismiss = { viewModel.dismissPaymentDialog() },
                onConfirmPayment = { paymentMethod ->
                    viewModel.saveOrder(paymentMethod)
                }
            )
        }

        selectedProduct?.let { product ->
            AddToCartDialog(
                product = product,
                allToppings = toppings,
                onDismiss = { viewModel.onDialogDismiss() },
                onAddToCart = { qty, selectedToppings ->
                    viewModel.addToCart(product, qty, selectedToppings)
                }
            )
        }
    }
}

@Composable
fun CategorySelector(categories: List<Category>, onCategorySelected: (Category) -> Unit) {
    LazyRow {
        items(categories) { category ->
            Button(
                onClick = { onCategorySelected(category) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(category.name)
            }
        }
    }
}

@Composable
fun PaymentDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Confirm Payment", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Total: ${String.format("$%.2f", total)}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onConfirmPayment("Card") }) {
                        Text("Card")
                    }
                    Button(onClick = { onConfirmPayment("Cash") }) {
                        Text("Cash")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun AddToCartDialog(
    product: Product,
    allToppings: List<Topping>,
    onDismiss: () -> Unit,
    onAddToCart: (Int, List<Topping>) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val selectedToppings = remember { mutableStateListOf<Topping>() }
    val applicableToppings = allToppings.filter { it.applicableTo.isEmpty() || it.applicableTo.contains(product.id) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quantity: $quantity")
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { quantity++ }) { Text("+") }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Toppings
                if (applicableToppings.isNotEmpty()) {
                    Text("Toppings:")
                    LazyColumn {
                        items(applicableToppings) { topping ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedToppings.contains(topping),
                                    onCheckedChange = {
                                        if (it) selectedToppings.add(topping) else selectedToppings.remove(topping)
                                    }
                                )
                                Text("${topping.name} ($${topping.price})")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onAddToCart(quantity, selectedToppings.toList()) }) { Text("Add to Cart") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductGrid(products: List<Product>, onProductSelected: (Product) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
        items(products) { product ->
            Card(
                onClick = { onProductSelected(product) },
                modifier = Modifier.padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // TODO: Load image
                    Text(text = product.name)
                    Text(text = String.format("$%.2f", product.price))
                }
            }
        }
    }
}

@Composable
fun SideCart(
    modifier: Modifier = Modifier,
    items: List<OrderItem>,
    subtotal: Double,
    tax: Double,
    total: Double,
    orderState: OrderState,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onPay: () -> Unit
) {
    Column(modifier = modifier
        .padding(16.dp)
        .fillMaxHeight()) {
        Text("Cart", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.qty} x ${item.name}")
                        Text(String.format("$%.2f", item.subtotal))
                    }
                    if (item.toppings.isNotEmpty()) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            item.toppings.forEach { topping ->
                                Text("+ ${topping.name}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal")
            Text(String.format("$%.2f", subtotal))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tax")
            Text(String.format("$%.2f", tax))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.headlineSmall)
            Text(String.format("$%.2f", total), style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Button(onClick = onClear, enabled = orderState !is OrderState.Saving) { Text("Clear") }
            Button(onClick = onSave, enabled = orderState !is OrderState.Saving) { Text("Save") }
        }
        Button(
            onClick = onPay,
            enabled = orderState !is OrderState.Saving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            if (orderState is OrderState.Saving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Pay")
            }
        }
        when (orderState) {
            is OrderState.Success -> {
                Text("Order saved successfully!", color = MaterialTheme.colorScheme.primary)
            }
            is OrderState.Error -> {
                Text(orderState.message, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}
