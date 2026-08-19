package com.example.kpop.network.model

data class CartRequest(val userId: Int, val productId: Int, val variantId: Int? = null, val quantity: Int)

data class ApiCartMutation(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val variantId: Int?,
    val quantity: Int
)

data class ApiCartItem(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val variantId: Int? = null,
    val variantName: String? = null,
    val name: String,
    val category: String? = null,
    val originalPrice: Double,
    val price: Double,
    val onSale: Boolean = false,
    val rating: Double? = 0.0,
    val image: String? = null,
    var quantity: Int
)