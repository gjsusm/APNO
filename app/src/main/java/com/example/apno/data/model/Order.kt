package com.example.apno.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "", // "cash", "card", "other"
    val status: String = "completed", // "completed", "cancelled"
    @ServerTimestamp
    val createdAt: Date? = null
)

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val toppings: List<Topping> = emptyList(),
    val qty: Int = 1,
    val price: Double = 0.0, // Price of the product at the time of sale
    val subtotal: Double = 0.0 // (price * qty) + toppings
)
