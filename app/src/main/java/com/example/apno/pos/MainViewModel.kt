package com.example.apno.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class OrderState {
    object Idle : OrderState()
    object Saving : OrderState()
    object Success : OrderState()
    data class Error(val message: String) : OrderState()
}

class MainViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _toppings = MutableStateFlow<List<Topping>>(emptyList())
    val toppings = _toppings.asStateFlow()

    private val _cartItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _showAddToCartDialog = MutableStateFlow<Product?>(null)
    val showAddToCartDialog = _showAddToCartDialog.asStateFlow()

    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog = _showPaymentDialog.asStateFlow()

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState = _orderState.asStateFlow()

    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.subtotal }

    // Assuming a fixed tax rate for now
    val tax: Double
        get() = cartSubtotal * 0.1

    val cartTotal: Double
        get() = cartSubtotal + tax

    init {
        fetchCategories()
        fetchToppings()
    }

    private fun fetchCategories() {
        db.collection("categories").orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                val categoryList = snapshot?.toObjects<Category>() ?: emptyList()
                _categories.value = categoryList
                if (categoryList.isNotEmpty() && products.value.isEmpty()) {
                    fetchProductsForCategory(categoryList.first().id)
                }
            }
    }

    fun fetchProductsForCategory(categoryId: String) {
        db.collection("products")
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                _products.value = snapshot?.toObjects() ?: emptyList()
            }
    }

    private fun fetchToppings() {
        db.collection("toppings").whereEqualTo("active", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                _toppings.value = snapshot?.toObjects() ?: emptyList()
            }
    }

    fun onProductSelected(product: Product) {
        _showAddToCartDialog.value = product
    }

    fun onDialogDismiss() {
        _showAddToCartDialog.value = null
    }

    fun showPaymentDialog() {
        if (cartItems.value.isNotEmpty()) {
            _showPaymentDialog.value = true
        }
    }

    fun dismissPaymentDialog() {
        _showPaymentDialog.value = false
    }

    fun addToCart(product: Product, quantity: Int, selectedToppings: List<Topping>) {
        val toppingsPrice = selectedToppings.sumOf { it.price }
        val subtotal = (product.price + toppingsPrice) * quantity

        val newItem = OrderItem(
            productId = product.id,
            name = product.name,
            qty = quantity,
            price = product.price,
            toppings = selectedToppings,
            subtotal = subtotal
        )
        _cartItems.value = _cartItems.value + newItem
        onDialogDismiss()
    }

    fun removeFromCart(item: OrderItem) {
        _cartItems.value = _cartItems.value - item
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _orderState.value = OrderState.Idle
    }

    fun saveOrder(paymentMethod: String) {
        viewModelScope.launch {
            dismissPaymentDialog()
            _orderState.value = OrderState.Saving
            val user = auth.currentUser
            if (user == null) {
                _orderState.value = OrderState.Error("User not logged in")
                return@launch
            }

            if (cartItems.value.isEmpty()) {
                _orderState.value = OrderState.Error("Cart is empty")
                return@launch
            }

            val order = Order(
                userId = user.uid,
                items = cartItems.value,
                subtotal = cartSubtotal,
                tax = tax,
                discount = 0.0, // Not implemented yet
                total = cartTotal,
                paymentMethod = paymentMethod,
                status = "completed"
            )

            try {
                db.collection("orders").add(order).await()
                _orderState.value = OrderState.Success
                clearCart()
            } catch (e: Exception) {
                _orderState.value = OrderState.Error(e.message ?: "Failed to save order")
            }
        }
    }
}
