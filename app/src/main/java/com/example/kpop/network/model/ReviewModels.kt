package com.example.kpop.network.model

data class ApiReview(
    val id: Int = 0,
    val userId: Int,
    val userName: String? = null,
    val productId: Int,
    val rating: Double,
    val comment: String,
    val date: String
)

data class ReviewRequest(
    val userId: Int,
    val productId: Int,
    val rating: Double,
    val comment: String,
    val date: String
)

data class ReviewEligibility(
    val eligible: Boolean,
    val reason: String
)