package com.example.kpop.network

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

data class ApiUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class LoginResponse(val token: String, val user: ApiUser)

data class ApiCategory(val id: Int, val name: String)

data class ApiGroup(
    val id: Int,
    val name: String,
    val company: String? = null,
    val debutDate: String? = null,
    val image: String? = null,
    val description: String? = null
)

data class ApiProduct(
    val id: Int,
    val name: String,
    val price: Double,
    val onSale: Boolean = false,
    val salePrice: Double? = null,
    val description: String? = null,
    val rating: Double? = 0.0,
    val quantity: Int = 0,
    val image: String? = null,
    val category: ApiCategory? = null,
    val group: ApiGroup? = null
) {
    fun currentPrice(): Double = if (onSale && salePrice != null) salePrice else price
}

data class ApiVariant(
    val id: Int,
    val name: String,
    val price: Double,
    val onSale: Boolean = false,
    val salePrice: Double? = null,
    val quantity: Int,
    val image: String? = null
)

data class ApiProductImage(
    val id: Int,
    val imageUrl: String
)

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

data class ApiWishlistItem(
    val wishlistId: Int,
    val productId: Int,
    val name: String,
    val price: Double,
    val category: String?,
    val image: String?
)

data class CartRequest(
    val userId: Int,
    val productId: Int,
    val variantId: Int? = null,
    val quantity: Int
)

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