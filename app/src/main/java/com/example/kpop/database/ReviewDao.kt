package com.example.kpop.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kpop.model.Review

@Dao
interface ReviewDao {

    @Insert
    fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE productId = :productId")
    fun getReviewsByProduct(productId: Int): MutableList<Review>

    @Query("SELECT AVG(rating) FROM reviews WHERE productId = :productId")
    fun getAverageRating(productId: Int): Float?

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    fun getReviewCount(productId: Int): Int

    @Query("""
        SELECT * FROM reviews
        WHERE userId = :userId
        AND productId = :productId
        LIMIT 1
    """)
    fun getUserReview(
        userId: Int,
        productId: Int
    ): Review?
}