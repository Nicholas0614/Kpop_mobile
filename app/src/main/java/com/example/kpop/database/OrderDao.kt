package com.example.kpop.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kpop.model.Order

@Dao
interface OrderDao {

    @Insert
    fun insertOrder(order: Order)

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY id DESC")
    fun getOrdersByUser(userId: Int): MutableList<Order>
}