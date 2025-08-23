package com.example.apno.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String? = null,
    val optionalStock: Int? = null
)
