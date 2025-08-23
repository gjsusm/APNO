package com.example.apno.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AuditLog(
    val id: String = "",
    val action: String = "", // e.g., "order_created", "product_updated"
    val userId: String = "",
    val targetCollection: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val details: Map<String, Any> = emptyMap()
)
