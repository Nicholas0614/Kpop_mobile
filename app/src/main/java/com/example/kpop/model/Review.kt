package com.example.kpop.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val userName: String,
    val productId: Int,
    val rating: Float,
    val comment: String,
    val date: String
)