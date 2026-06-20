package com.example.kpop.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val name: String,
    val category: String,
    val price: Int,
    val rating: Double,
    val image: Int,
    var quantity: Int
)