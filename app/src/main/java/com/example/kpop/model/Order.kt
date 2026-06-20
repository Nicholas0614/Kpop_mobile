package com.example.kpop.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val productId: Int,

    val name: String,
    val category: String,
    val price: Double,
    val image: Int,

    val quantity: Int,
    val totalPrice: Double,
    val date: String
)