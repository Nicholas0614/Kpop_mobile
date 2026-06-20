package com.example.kpop.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.kpop.model.Product

@Dao
interface ProductDao {

    @Insert
    fun insertProduct(product: Product)

    @Insert
    fun insertProducts(products: List<Product>)

    @Update
    fun updateProduct(product: Product)

    @Delete
    fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): List<Product>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Int

    @Query("UPDATE products SET rating = :rating WHERE id = :productId")
    fun updateProductRating(productId: Int, rating: Double)
}