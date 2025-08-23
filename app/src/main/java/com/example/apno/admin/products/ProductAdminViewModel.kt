package com.example.apno.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.Category
import com.example.apno.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductAdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")
    private val categoriesCollection = db.collection("categories")

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    init {
        listenForProductChanges()
        fetchCategories()
    }

    private fun listenForProductChanges() {
        productsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _products.value = snapshot?.toObjects() ?: emptyList()
        }
    }

    private fun fetchCategories() {
        categoriesCollection.orderBy("name").addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _categories.value = snapshot?.toObjects() ?: emptyList()
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            try {
                val snapshot = productsCollection.document(productId).get().await()
                _selectedProduct.value = snapshot.toObject<Product>()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetSelectedProduct() {
        _selectedProduct.value = null
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            try {
                if (product.id.isBlank()) {
                    productsCollection.add(product).await()
                } else {
                    productsCollection.document(product.id).set(product).await()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                productsCollection.document(productId).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
