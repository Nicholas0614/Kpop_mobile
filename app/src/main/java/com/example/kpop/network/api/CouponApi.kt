package com.example.kpop.network.api

import com.example.kpop.network.model.ApiCoupon
import com.example.kpop.network.model.CouponRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CouponApi {
    @GET("admin/coupons")
    fun getCoupons():Call<List<ApiCoupon>>

    @POST("coupons")
    fun addCoupon(@Body request:CouponRequest):Call<ApiCoupon>

    @PUT("coupons/{id}")
    fun updateCoupon(@Path("id") id:Int,@Body request:CouponRequest):Call<ApiCoupon>

    @DELETE("coupons/{id}")
    fun deleteCoupon(@Path("id") id:Int):Call<ResponseBody>
}