package com.example.kpop.network.model

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class ApiUser(val id: Int, val name: String, val email: String, val role: String)
data class LoginResponse(val token: String, val user: ApiUser)