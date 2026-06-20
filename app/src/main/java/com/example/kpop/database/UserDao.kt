package com.example.kpop.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kpop.model.User

@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    fun login(email: String, password: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    fun checkEmailExists(email: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE role='admin'")
    fun getAdminCount(): Int

    @Query("SELECT * FROM users WHERE role='admin' LIMIT 1")
    fun getAdmin(): User?
}