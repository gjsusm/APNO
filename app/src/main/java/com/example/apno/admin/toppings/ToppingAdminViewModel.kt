package com.example.apno.admin.toppings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.Topping
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ToppingAdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val toppingsCollection = db.collection("toppings")

    private val _toppings = MutableStateFlow<List<Topping>>(emptyList())
    val toppings = _toppings.asStateFlow()

    init {
        listenForToppingChanges()
    }

    private fun listenForToppingChanges() {
        toppingsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _toppings.value = snapshot?.toObjects() ?: emptyList()
        }
    }

    fun addTopping(name: String, price: Double) {
        viewModelScope.launch {
            try {
                val topping = Topping(name = name, price = price)
                toppingsCollection.add(topping).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTopping(topping: Topping) {
        viewModelScope.launch {
            try {
                toppingsCollection.document(topping.id).set(topping).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTopping(toppingId: String) {
        viewModelScope.launch {
            try {
                toppingsCollection.document(toppingId).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
