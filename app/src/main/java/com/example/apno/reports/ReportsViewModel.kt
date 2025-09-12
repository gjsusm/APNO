package com.example.apno.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class SalesReport(
    val totalSales: Double = 0.0,
    val numberOfOrders: Int = 0,
    val orders: List<Order> = emptyList()
)

class ReportsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _salesReport = MutableStateFlow<SalesReport?>(null)
    val salesReport = _salesReport.asStateFlow()

    fun generateSalesReport(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("orders")
                    .whereGreaterThanOrEqualTo("createdAt", startDate)
                    .whereLessThanOrEqualTo("createdAt", endDate)
                    .get()
                    .await()

                val orders = snapshot.toObjects<Order>()
                val totalSales = orders.sumOf { it.total }
                val numberOfOrders = orders.size

                _salesReport.value = SalesReport(
                    totalSales = totalSales,
                    numberOfOrders = numberOfOrders,
                    orders = orders
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
