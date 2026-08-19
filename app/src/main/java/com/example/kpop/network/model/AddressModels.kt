package com.example.kpop.network.model

data class ApiAddress(
    val id: Int,
    val userId: Int,
    val label: String,
    val recipientName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val postcode: String,
    val country: String,
    val defaultAddress: Boolean = false
)

data class AddressRequest(
    val userId: Int,
    val label: String,
    val recipientName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val postcode: String,
    val country: String = "Malaysia",
    val defaultAddress: Boolean = false
)