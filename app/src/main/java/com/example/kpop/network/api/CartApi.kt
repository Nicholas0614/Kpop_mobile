package com.example.kpop.network.api

import com.example.kpop.network.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CartApi {

    @GET("cart/user/{userId}")
    fun getCart(@Path("userId") userId: Int): Call<List<ApiCartItem>>

    @POST("cart")
    fun addCart(@Body request: CartRequest): Call<ApiCartMutation>

    @PUT("cart/{id}")
    fun updateCart(@Path("id") id: Int, @Body request: CartRequest): Call<ResponseBody>

    @DELETE("cart/{id}")
    fun deleteCart(@Path("id") id: Int): Call<ResponseBody>
}