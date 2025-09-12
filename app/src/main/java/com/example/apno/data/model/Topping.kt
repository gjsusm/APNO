package com.example.apno.data.model

data class Topping(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val applicableTo: List<String> = emptyList(), // List of product IDs
    val active: Boolean = true
)
