package com.example.apno.data.model

data class Settings(
    val storeName: String = "",
    val address: String = "",
    val ruc: String = "", // tax id
    val currency: String = "USD",
    val taxPercent: Double = 0.0,
    val printerConfig: Map<String, String> = emptyMap() // e.g., {"type": "bluetooth", "address": "00:11:22:33:44:55"}
)
