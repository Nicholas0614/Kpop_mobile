package com.example.kpop.network.api

import com.example.kpop.network.model.AddressRequest
import com.example.kpop.network.model.ApiAddress
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface AddressApi {

    @GET("addresses/user/{userId}")
    fun getAddresses(@Path("userId") userId: Int): Call<List<ApiAddress>>

    @GET("addresses/{id}")
    fun getAddress(@Path("id") id: Int): Call<ApiAddress>

    @POST("addresses")
    fun addAddress(@Body request: AddressRequest): Call<ResponseBody>

    @PUT("addresses/{id}")
    fun updateAddress(@Path("id") id:Int,@Body request:AddressRequest):Call<ApiAddress>

    @PUT("addresses/user/{userId}/default/{addressId}")
    fun setDefault(@Path("userId") userId: Int, @Path("addressId") addressId: Int): Call<ApiAddress>

    @DELETE("addresses/{id}")
    fun deleteAddress(@Path("id") id: Int): Call<ResponseBody>
}