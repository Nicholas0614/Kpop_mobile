package com.example.kpop.network.api

import com.example.kpop.network.model.ApiOrder
import com.example.kpop.network.model.CheckoutResponse
import retrofit2.Call
import retrofit2.http.*

interface OrderApi{

    @POST("orders/checkout/{userId}")
    fun checkout(@Path("userId") userId:Int,@Query("addressId") addressId:Int,@Query("couponCode") couponCode:String?=null,@Query("cartIds") cartIds:List<Int>):Call<CheckoutResponse>

    @GET("orders/user/{userId}")
    fun getOrders(@Path("userId") userId:Int):Call<List<ApiOrder>>

    @PUT("orders/{id}/received")
    fun confirmReceived(@Path("id") id:Int):Call<ApiOrder>
}