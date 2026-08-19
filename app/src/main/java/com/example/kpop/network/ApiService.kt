package com.example.kpop.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("users/register")
    fun register(@Body request: RegisterRequest): Call<ApiUser>

    @GET("products")
    fun getProducts(): Call<List<ApiProduct>>

    @GET("products/{id}")
    fun getProduct(@Path("id") id: Int): Call<ApiProduct>

    @GET("products/search")
    fun searchProducts(
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("groupId") groupId: Int? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null
    ): Call<List<ApiProduct>>

    @GET("variants/product/{productId}")
    fun getVariants(@Path("productId") productId: Int): Call<List<ApiVariant>>

    @GET("product-images/product/{productId}")
    fun getProductImages(@Path("productId") productId: Int): Call<List<ApiProductImage>>

    @GET("reviews/product/{productId}")
    fun getReviews(@Path("productId") productId: Int): Call<List<ApiReview>>

    @POST("reviews")
    fun addReview(@Body request: ReviewRequest): Call<ApiReview>

    @GET("wishlist/{userId}")
    fun getWishlist(@Path("userId") userId: Int): Call<List<ApiWishlistItem>>

    @POST("wishlist")
    fun addWishlist(@Query("userId") userId: Int, @Query("productId") productId: Int): Call<ResponseBody>

    @DELETE("wishlist")
    fun removeWishlist(@Query("userId") userId: Int, @Query("productId") productId: Int): Call<ResponseBody>

    @POST("cart")
    fun addCart(@Body request: CartRequest): Call<ApiCartMutation>

    @GET("cart/user/{userId}")
    fun getCart(@Path("userId") userId: Int): Call<List<ApiCartItem>>

    @PUT("cart/{id}")
    fun updateCart(@Path("id") id: Int, @Body request: CartRequest): Call<ResponseBody>

    @DELETE("cart/{id}")
    fun deleteCart(@Path("id") id: Int): Call<ResponseBody>
}