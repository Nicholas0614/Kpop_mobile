package com.example.kpop.network.model

data class ApiWishlistItem(
    val wishlistId: Int,
    val productId: Int,
    val name: String,
    val price: Double,
    val category: String?,
    val image: String?
)