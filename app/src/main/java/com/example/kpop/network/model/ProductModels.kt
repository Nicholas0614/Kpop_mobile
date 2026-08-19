package com.example.kpop.network.model

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

data class ProductRequest(
    val name: String,
    val price: Double,
    val onSale: Boolean = false,
    val salePrice: Double? = null,
    val description: String? = null,
    val rating: Double? = 0.0,
    val quantity: Int,
    val image: String? = null,
    val category: ApiCategory,
    val group: ApiGroup
)

data class ApiVariant(
    val id: Int,
    val name: String,
    val price: Double,
    val onSale: Boolean = false,
    val salePrice: Double? = null,
    val quantity: Int,
    val image: String? = null
)

data class VariantRequest(
    val name: String,
    val price: Double,
    val onSale: Boolean = false,
    val salePrice: Double? = null,
    val quantity: Int,
    val image: String? = null
)

data class ApiProductImage(val id: Int, val imageUrl: String)

data class ProductImageRequest(val imageUrl: String)