package com.example.kpop.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.kpop.model.CartEntity

@Dao
interface CartDao {

    @Query("SELECT * FROM cart WHERE userId = :userId")
    fun getCartItems(userId: Int): MutableList<CartEntity>

    @Query("SELECT * FROM cart WHERE userId = :userId AND productId = :productId LIMIT 1")
    fun findCartItem(userId: Int, productId: Int): CartEntity?

    @Insert
    fun insertCartItem(cartItem: CartEntity)

    @Update
    fun updateCartItem(cartItem: CartEntity)

    @Delete
    fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM cart WHERE userId = :userId")
    fun clearCart(userId: Int)

    @Query("SELECT IFNULL(SUM(quantity), 0) FROM cart WHERE userId = :userId")
    fun getCartCount(userId: Int): Int
}