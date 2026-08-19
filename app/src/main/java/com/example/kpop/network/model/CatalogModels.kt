package com.example.kpop.network.model

data class CategoryRequest(
    val name: String
)

data class GroupRequest(
    val name: String,
    val company: String? = null,
    val debutDate: String? = null,
    val image: String? = null,
    val description: String? = null
)