package com.example.kpop.network

import android.content.Context
import com.example.kpop.network.model.ApiUser

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(token: String, user: ApiUser) {
        prefs.edit()
            .putString("token", token)
            .putInt("userId", user.id)
            .putString("name", user.name)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getUserId(): Int {
        return prefs.getInt("userId", 0)
    }

    fun getName(): String? {
        return prefs.getString("name", null)
    }

    fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}