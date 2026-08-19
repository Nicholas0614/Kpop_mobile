package com.example.kpop.network.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @POST("payment/paypal/capture/{id}")
    fun capturePayment(@Path("id") paypalOrderId: String): Call<ResponseBody>
}