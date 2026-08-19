package com.example.kpop.network.api

import com.example.kpop.network.model.ApiWishlistItem
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface WishlistApi {

    @GET("wishlist/{userId}")
    fun getWishlist(@Path("userId") userId: Int): Call<List<ApiWishlistItem>>

    @POST("wishlist")
    fun addWishlist(@Query("userId") userId: Int, @Query("productId") productId: Int): Call<ResponseBody>

    @DELETE("wishlist")
    fun removeWishlist(@Query("userId") userId: Int, @Query("productId") productId: Int): Call<ResponseBody>
}