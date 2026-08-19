package com.example.kpop.network.model

data class ApiOrderItem(
    val id: Int = 0,
    val productId: Int,
    val productName: String,
    val variantId: Int? = null,
    val variantName: String? = null,
    val quantity: Int,
    val price: Double
)

data class ApiOrder(
    val id: Int,
    val userId: Int,
    val totalPrice: Double = 0.0,
    val couponCode: String? = null,
    val discountAmount: Double = 0.0,
    val finalPrice: Double = 0.0,
    val date: String? = null,
    val paymentStatus: String? = null,
    val paypalOrderId: String? = null,
    val orderStatus: String? = null,
    val trackingNumber: String? = null,
    val addressId: Int? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    val items: List<ApiOrderItem> = emptyList()
)

data class CheckoutResponse(
    val order: ApiOrder,
    val items: List<ApiOrderItem> = emptyList(),
    val paypalApprovalUrl: String
)