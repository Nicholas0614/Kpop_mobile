package com.example.kpop.network.model

data class AdminDashboard(
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val productsOnSale: Int = 0,
    val totalOrders: Int = 0,
    val paidOrders: Int = 0,
    val processingOrders: Int = 0,
    val shippedOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val totalRevenue: Double = 0.0
)