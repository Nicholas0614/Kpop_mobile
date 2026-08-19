package com.example.kpop.network.api

import com.example.kpop.network.model.UserCoupon
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserCouponApi {
    @GET("user-coupons")
    fun getCoupons():Call<List<UserCoupon>>

    @GET("user-coupons/saved")
    fun getSavedCoupons():Call<List<UserCoupon>>

    @POST("user-coupons/{couponId}")
    fun saveCoupon(@Path("couponId") couponId:Int):Call<ResponseBody>

    @DELETE("user-coupons/{couponId}")
    fun removeCoupon(@Path("couponId") couponId:Int):Call<ResponseBody>
}